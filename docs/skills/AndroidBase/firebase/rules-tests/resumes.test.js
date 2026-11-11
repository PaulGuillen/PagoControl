import { readFile } from "node:fs/promises";
import { after, before, beforeEach, test } from "node:test";
import { assertFails, assertSucceeds, initializeTestEnvironment } from "@firebase/rules-unit-testing";
import { collection, deleteDoc, doc, getDoc, getDocs, serverTimestamp, setDoc } from "firebase/firestore";

let environment;
const path = "users/owner/resumes/cv-one";
const payload = () => ({ schemaVersion: "1", content: '{"id":"cv-one"}', updatedAt: 1, syncedAt: serverTimestamp() });

before(async () => {
  const fragment = await readFile(new URL("../resumes.rules", import.meta.url), "utf8");
  environment = await initializeTestEnvironment({
    projectId: "demo-cvfree-rules",
    firestore: { host: "127.0.0.1", port: 8180, rules: `rules_version = '2'; service cloud.firestore { match /databases/{database}/documents { ${fragment} } }` },
  });
});
beforeEach(async () => environment.clearFirestore());
after(async () => environment?.cleanup());

test("owner can create, list, read, update and delete resumes", async () => {
  const database = environment.authenticatedContext("owner").firestore();
  const reference = doc(database, path);
  await assertSucceeds(setDoc(reference, payload()));
  await assertSucceeds(getDoc(reference));
  await assertSucceeds(getDocs(collection(database, "users/owner/resumes")));
  await assertSucceeds(setDoc(reference, { ...payload(), updatedAt: 2 }));
  await assertSucceeds(deleteDoc(reference));
});

test("another account cannot read, list, write or delete an owner's resumes", async () => {
  await environment.withSecurityRulesDisabled(async context => setDoc(doc(context.firestore(), path), payload()));
  const database = environment.authenticatedContext("intruder").firestore();
  await assertFails(getDoc(doc(database, path)));
  await assertFails(getDocs(collection(database, "users/owner/resumes")));
  await assertFails(setDoc(doc(database, path), payload()));
  await assertFails(deleteDoc(doc(database, path)));
});

test("signed-out clients cannot read or write resumes", async () => {
  const database = environment.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(database, path)));
  await assertFails(setDoc(doc(database, path), payload()));
  await assertFails(deleteDoc(doc(database, path)));
});

test("invalid fields, schema, timestamps and oversized content are rejected", async () => {
  const reference = doc(environment.authenticatedContext("owner").firestore(), path);
  for (const invalid of [
    { ...payload(), schemaVersion: "2" },
    { ...payload(), content: "" },
    { ...payload(), content: "x".repeat(750001) },
    { ...payload(), updatedAt: -1 },
    { ...payload(), updatedAt: "1" },
    { ...payload(), syncedAt: new Date(0) },
    { ...payload(), extra: true },
    { content: "{}" },
  ]) {
    await assertFails(setDoc(reference, invalid));
  }
});
