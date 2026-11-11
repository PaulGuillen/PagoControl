const test = require("node:test");
const assert = require("node:assert/strict");
const { WEEK_MS, RESERVATION_MS, availability, enhanceWithQuota, readQuota, reserveQuota, settleQuota } = require("../src/quota");

function memoryStore() {
  const records = new Map();
  let queue = Promise.resolve();
  return {
    records,
    collection: () => ({ doc: (id) => ({ id }) }),
    runTransaction: (operation) => {
      const result = queue.then(async () => {
        const writes = new Map();
        const value = await operation({
          get: async (reference) => ({ data: () => structuredClone(records.get(reference.id)) }),
          set: (reference, data) => writes.set(reference.id, structuredClone(data)),
        });
        for (const [id, data] of writes) records.set(id, data);
        return value;
      });
      queue = result.catch(() => {});
      return result;
    },
  };
}

test("a new device has one AI use and the same device is blocked until exactly seven days", async () => {
  const store = memoryStore();
  let timestamp = 100000;
  let calls = 0;
  const request = () => enhanceWithQuota({
    store, deviceHash: "device", premium: false, now: () => timestamp,
    generate: async () => { calls += 1; return "resume"; },
  });
  assert.equal((await request()).status, "enhanced");
  timestamp += WEEK_MS - 1;
  assert.deepEqual(await request(), { status: "limited", nextAvailableAtMs: 100000 + WEEK_MS });
  assert.equal(calls, 1);
  timestamp += 1;
  assert.equal((await request()).status, "enhanced");
  assert.equal(calls, 2);
});

test("the cooldown starts on successful completion using server time", async () => {
  const store = memoryStore();
  let timestamp = 1000;
  await enhanceWithQuota({
    store, deviceHash: "device", now: () => timestamp,
    generate: async () => { timestamp = 5000; return "resume"; },
  });
  assert.equal(store.records.get("device").lastSuccessAtMs, 5000);
});

test("parallel PDF and scan requests on the same device only run AI once", async () => {
  const store = memoryStore();
  let calls = 0;
  const request = () => enhanceWithQuota({
    store, deviceHash: "device", now: () => 1000,
    generate: async () => { calls += 1; return "resume"; },
  });
  const results = await Promise.all([request(), request()]);
  assert.equal(calls, 1);
  assert.equal(results.filter((result) => result.status === "enhanced").length, 1);
  assert.equal(results.filter((result) => ["busy", "limited"].includes(result.status)).length, 1);
});

test("AI failure releases the reservation without consuming a week", async () => {
  const store = memoryStore();
  await assert.rejects(enhanceWithQuota({
    store, deviceHash: "device", now: () => 1000,
    generate: async () => { throw new Error("timeout"); },
  }));
  assert.equal(availability(store.records.get("device"), 1000).status, "available");
});

test("a device keeps its cooldown across fresh calls and other accounts", async () => {
  const store = memoryStore();
  const first = await enhanceWithQuota({ store, deviceHash: "same-device", now: () => 1000, generate: async () => "resume" });
  assert.equal(first.status, "enhanced");
  const second = await enhanceWithQuota({
    store, deviceHash: "same-device", now: () => 2000,
    generate: async () => { assert.fail("AI must not run again"); },
  });
  assert.equal(second.status, "limited");
});

test("different devices have independent allowances", async () => {
  const store = memoryStore();
  for (const deviceHash of ["first", "second"]) {
    assert.equal((await enhanceWithQuota({ store, deviceHash, now: () => 1000, generate: async () => "resume" })).status, "enhanced");
  }
});

test("a verified premium entitlement skips the weekly quota", async () => {
  const store = memoryStore();
  for (let attempt = 0; attempt < 2; attempt += 1) {
    assert.equal((await enhanceWithQuota({ store, deviceHash: "device", premium: true, generate: async () => "resume" })).status, "enhanced");
  }
  assert.equal(store.records.size, 0);
});

test("an interrupted server reservation expires without consuming a week", async () => {
  const store = memoryStore();
  await reserveQuota(store, "device", () => 1000);
  assert.equal(availability(store.records.get("device"), 1000 + RESERVATION_MS - 1).status, "busy");
  assert.equal(availability(store.records.get("device"), 1000 + RESERVATION_MS).status, "available");
});

test("an expired request cannot release or consume a newer reservation", async () => {
  const store = memoryStore();
  const first = await reserveQuota(store, "device", () => 1000);
  const second = await reserveQuota(store, "device", () => 1000 + RESERVATION_MS);
  assert.equal(await settleQuota(store, first, false), false);
  assert.equal(await settleQuota(store, first, true), false);
  assert.equal(store.records.get("device").reservationId, second.reservationId);
});

test("checking availability never consumes a new device allowance", async () => {
  const store = memoryStore();
  for (let attempt = 0; attempt < 3; attempt += 1) {
    assert.deepEqual(await readQuota({ store, deviceHash: "device", now: () => 1000 }), { status: "available" });
  }
  assert.equal(store.records.size, 0);
});

test("the status query shows the persisted seven-day deadline without extending it", async () => {
  const store = memoryStore();
  await enhanceWithQuota({ store, deviceHash: "device", now: () => 1000, generate: async () => "resume" });
  assert.deepEqual(await readQuota({ store, deviceHash: "device", now: () => 2000 }), {
    status: "limited", nextAvailableAtMs: 1000 + WEEK_MS,
  });
  assert.deepEqual(await readQuota({ store, deviceHash: "device", now: () => 1000 + WEEK_MS - 1 }), {
    status: "limited", nextAvailableAtMs: 1000 + WEEK_MS,
  });
  assert.deepEqual(await readQuota({ store, deviceHash: "device", now: () => 1000 + WEEK_MS }), { status: "available" });
  assert.equal(store.records.get("device").lastSuccessAtMs, 1000);
});

test("checking a busy device does not acquire or replace its reservation", async () => {
  const store = memoryStore();
  const reservation = await reserveQuota(store, "device", () => 1000);
  assert.deepEqual(await readQuota({ store, deviceHash: "device", now: () => 2000 }), {
    status: "busy", nextAvailableAtMs: 1000 + RESERVATION_MS,
  });
  assert.equal(store.records.get("device").reservationId, reservation.reservationId);
});

test("trusted premium status is available even when the device has a free cooldown", async () => {
  const store = memoryStore();
  await enhanceWithQuota({ store, deviceHash: "device", now: () => 1000, generate: async () => "resume" });
  assert.deepEqual(await readQuota({ store, deviceHash: "device", premium: true, now: () => 2000 }), { status: "available" });
  assert.equal(store.records.get("device").lastSuccessAtMs, 1000);
});
