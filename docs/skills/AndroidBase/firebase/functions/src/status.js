function serverStatus(uid, now) {
  if (typeof uid !== "string" || uid.trim().length === 0) {
    throw new Error("unauthenticated");
  }
  return { status: "ready", serverTimeMs: now };
}

module.exports = { serverStatus };
