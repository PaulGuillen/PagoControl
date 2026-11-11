const { randomUUID } = require("node:crypto");

const WEEK_MS = 7 * 24 * 60 * 60 * 1000;
const RESERVATION_MS = 5 * 60 * 1000;

function availability(data, now) {
  if (typeof data?.lastSuccessAtMs === "number" && now < data.lastSuccessAtMs + WEEK_MS) {
    return { status: "limited", nextAvailableAtMs: data.lastSuccessAtMs + WEEK_MS };
  }
  if (typeof data?.reservedUntilMs === "number" && now < data.reservedUntilMs) {
    return { status: "busy", nextAvailableAtMs: data.reservedUntilMs };
  }
  return { status: "available" };
}

async function reserveQuota(store, deviceHash, now = Date.now) {
  const reference = store.collection("ai_device_quotas").doc(deviceHash);
  const reservationId = randomUUID();
  return store.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(reference);
    const data = snapshot.data();
    const timestamp = now();
    const result = availability(data, timestamp);
    if (result.status !== "available") return result;
    transaction.set(reference, {
      lastSuccessAtMs: data?.lastSuccessAtMs ?? null,
      reservationId,
      reservedUntilMs: timestamp + RESERVATION_MS,
    });
    return { status: "reserved", reference, reservationId };
  });
}

async function settleQuota(store, reservation, succeeded, now = Date.now) {
  return store.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(reservation.reference);
    const data = snapshot.data();
    if (data?.reservationId !== reservation.reservationId) return false;
    transaction.set(reservation.reference, {
      lastSuccessAtMs: succeeded ? now() : data.lastSuccessAtMs,
      reservationId: null,
      reservedUntilMs: 0,
    });
    return true;
  });
}

async function readQuota({ store, deviceHash, premium, now = Date.now }) {
  if (premium) return { status: "available" };
  const reference = store.collection("ai_device_quotas").doc(deviceHash);
  return store.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(reference);
    return availability(snapshot.data(), now());
  });
}

async function enhanceWithQuota({ store, deviceHash, premium, generate, now = Date.now }) {
  if (premium) return { status: "enhanced", resumeJson: await generate() };
  const reservation = await reserveQuota(store, deviceHash, now);
  if (reservation.status !== "reserved") return reservation;
  try {
    const resumeJson = await generate();
    const committed = await settleQuota(store, reservation, true, now);
    if (!committed) throw new Error("Reservation expired");
    return { status: "enhanced", resumeJson };
  } catch (error) {
    await settleQuota(store, reservation, false, now);
    throw error;
  }
}

module.exports = { WEEK_MS, RESERVATION_MS, availability, enhanceWithQuota, readQuota, reserveQuota, settleQuota };
