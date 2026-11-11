const test = require("node:test");
const assert = require("node:assert/strict");
const { generateResume, matchesSchema, responseSchema, validateInput } = require("../src/resume");

const input = { deviceHash: "a".repeat(64), source: "Resume content ".repeat(10), localDraft: "{}" };
const validResume = {
  contact: { name: "Paul", lastName: "", professionalTitle: "", email: "", phone: "", city: "", address: "" },
  experiences: [], education: [], skills: [], summary: "", courses: [], links: [], languages: [], references: [],
};
const response = (text, finishReason = "STOP") => ({
  ok: true,
  json: async () => ({ candidates: [{ finishReason, content: { parts: [{ text }] } }] }),
});
const generate = (fetchImpl) => generateResume({ ...input, apiKey: "secret", model: "configured-model", fetchImpl });

test("accepts bounded source text and a hashed device identifier", () => {
  assert.equal(validateInput(input), input);
});

test("rejects missing, malformed or raw device identifiers", () => {
  for (const deviceHash of [undefined, "raw-android-id", "", "A".repeat(64)]) {
    assert.throws(() => validateInput({ ...input, deviceHash }));
  }
});

test("rejects oversized and malformed payloads before using a quota", () => {
  for (const changes of [{ source: "short" }, { source: "a".repeat(60001) }, { localDraft: "a".repeat(120001) }, { localDraft: "[]" }, { localDraft: "invalid" }]) {
    assert.throws(() => validateInput({ ...input, ...changes }));
  }
});

test("requires every response field to have a compatible type", () => {
  assert.equal(matchesSchema(validResume, responseSchema), true);
  assert.equal(matchesSchema({ ...validResume, education: "invalid" }, responseSchema), false);
  assert.equal(matchesSchema({ ...validResume, contact: { name: 123 } }, responseSchema), false);
});

test("sends the server API key as a header and preserves month dates", async () => {
  const resume = { ...validResume, experiences: [{ position: "Developer", company: "Company", city: "", startYear: "07/2026", endYear: "Actualidad", achievements: "" }] };
  const result = await generate(async (url, options) => {
    assert.equal(url.includes("secret"), false);
    assert.equal(options.headers["x-goog-api-key"], "secret");
    assert.equal(JSON.parse(options.body).generationConfig.responseMimeType, "application/json");
    return response(JSON.stringify(resume));
  });
  assert.equal(JSON.parse(result).experiences[0].startYear, "07/2026");
});

test("rejects invalid, truncated and empty AI responses so the quota can be released", async () => {
  for (const result of [response("invalid"), response("{}"), response(JSON.stringify(validResume), "MAX_TOKENS"), response(JSON.stringify({ ...validResume, contact: { ...validResume.contact, name: "" } })), { ok: false }]) {
    await assert.rejects(generate(async () => result));
  }
});
