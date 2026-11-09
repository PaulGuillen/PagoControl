const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { serverStatus } = require("./status");

exports.getServerStatus = onCall(
  { region: "us-central1", enforceAppCheck: true, maxInstances: 5 },
  (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Authentication required");
    return serverStatus(request.auth.uid, Date.now());
  },
);
