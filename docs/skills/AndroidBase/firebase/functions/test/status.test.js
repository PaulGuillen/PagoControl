const test = require("node:test");
const assert = require("node:assert/strict");
const { serverStatus } = require("../src/status");

test("requires an authenticated identity", () => {
  for (const uid of [null, undefined, "", "   ", 123]) {
    assert.throws(() => serverStatus(uid, 100), /unauthenticated/);
  }
});

test("returns server time without exposing user identity", () => {
  assert.deepEqual(serverStatus("test-user", 123456), { status: "ready", serverTimeMs: 123456 });
});
