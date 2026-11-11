const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { defineSecret, defineString } = require("firebase-functions/params");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { enhanceWithQuota, readQuota } = require("./quota");
const { generateResume, validateInput } = require("./resume");

initializeApp();
const geminiApiKey = defineSecret("GEMINI_API_KEY");
const aiModelName = defineString("AI_MODEL_NAME");

exports.getResumeAiQuota = onCall({
  region: "us-central1",
  enforceAppCheck: true,
  timeoutSeconds: 15,
  maxInstances: 5,
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in to check AI availability");
  const deviceHash = request.data?.deviceHash;
  if (typeof deviceHash !== "string" || !/^[a-f0-9]{64}$/.test(deviceHash)) {
    throw new HttpsError("invalid-argument", "Invalid device identifier");
  }
  try {
    return await readQuota({
      store: getFirestore(),
      deviceHash,
      premium: request.auth.token.premium === true,
    });
  } catch {
    throw new HttpsError("unavailable", "AI availability could not be verified");
  }
});

exports.enhanceResume = onCall({
  region: "us-central1",
  enforceAppCheck: true,
  secrets: [geminiApiKey],
  timeoutSeconds: 90,
  memory: "256MiB",
  maxInstances: 5,
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in to use AI");
  let input;
  try {
    input = validateInput(request.data);
  } catch {
    throw new HttpsError("invalid-argument", "Invalid resume request");
  }
  try {
    return await enhanceWithQuota({
      store: getFirestore(),
      deviceHash: input.deviceHash,
      premium: request.auth.token.premium === true,
      generate: () => generateResume({
        source: input.source,
        localDraft: input.localDraft,
        model: aiModelName.value(),
        apiKey: geminiApiKey.value(),
      }),
    });
  } catch {
    throw new HttpsError("unavailable", "AI temporarily unavailable; use local import");
  }
});
