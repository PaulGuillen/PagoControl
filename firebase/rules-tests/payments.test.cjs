const { before, after, beforeEach, test } = require('node:test');
const { createHash } = require('node:crypto');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { initializeTestEnvironment, assertSucceeds, assertFails } = require('@firebase/rules-unit-testing');
const { doc, getDoc, collection, getDocs, query, where, orderBy, limit, writeBatch, setDoc, deleteDoc, serverTimestamp } = require('firebase/firestore');

let env;
const id = createHash('sha256').update('payment').digest('hex');
const headPath = 'users/alice/paymentSync/state';
const paymentPath = `users/alice/payments/${id}`;
const payment = (extra = {}) => ({
  schemaVersion: 1, id: 'payment', packageName: 'com.google.android.gm',
  source: 'Gmail', title: 'Bank', text: 'Receipt', postedAt: 123,
  suggestedAmountCents: 100, amountCents: 100, status: 'confirmed',
  revision: 1, writeId: 'write-1', updatedAt: serverTimestamp(), ...extra,
});
const metadata = (revision = 1) => ({ schemaVersion: 1, revision, updatedAt: serverTimestamp() });
function save(db, extra = {}, revision = 1) {
  const batch = writeBatch(db);
  batch.set(doc(db, headPath), metadata(revision));
  batch.set(doc(db, paymentPath), payment({ revision, ...extra }));
  return batch.commit();
}
before(async () => {
  const [host, port] = (process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8080').split(':');
  env = await initializeTestEnvironment({
    projectId: 'demo-pagocontrol-sync',
    firestore: { host, port: Number(port), rules: readFileSync(resolve(__dirname, '../firestore.rules'), 'utf8') },
  });
});
beforeEach(async () => env.clearFirestore());
after(async () => env.cleanup());

test('owner can write atomically and query incremental pages', async () => {
  const db = env.authenticatedContext('alice').firestore();
  await assertSucceeds(save(db));
  await assertSucceeds(getDoc(doc(db, headPath)));
  await assertSucceeds(getDocs(query(collection(db, 'users/alice/payments'), where('revision', '>', 0), where('revision', '<=', 1), orderBy('revision'), limit(100))));
});
test('a twenty-movement batch stays within revision and rule-access limits', async () => {
  const db = env.authenticatedContext('alice').firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, headPath), metadata(20));
  for (let i = 1; i <= 20; i++) {
    const originalId = `payment-${i}`;
    const documentId = createHash('sha256').update(originalId).digest('hex');
    batch.set(doc(db, `users/alice/payments/${documentId}`), payment({ id: originalId, revision: i }));
  }
  await assertSucceeds(batch.commit());
});
test('anonymous and another account cannot read or write movements or metadata', async () => {
  for (const db of [env.unauthenticatedContext().firestore(), env.authenticatedContext('bob').firestore()]) {
    await assertFails(save(db));
    await assertFails(getDoc(doc(db, paymentPath)));
    await assertFails(getDoc(doc(db, headPath)));
    await assertFails(getDocs(collection(db, 'users/alice/payments')));
  }
});
test('movement cannot be written without advancing metadata', async () => {
  const db = env.authenticatedContext('alice').firestore();
  await assertFails(setDoc(doc(db, paymentPath), payment()));
  await assertSucceeds(save(db));
  await assertFails(setDoc(doc(db, paymentPath), payment({ revision: 2 })));
});
test('invalid schema amounts text and extra fields are rejected', async () => {
  const db = env.authenticatedContext('alice').firestore();
  for (const extra of [{ schemaVersion: 2 }, { amountCents: 0 }, { amountCents: 1.5 }, { amountCents: 100000000000 }, { status: 'invented' }, { text: 'x'.repeat(4001) }, { privateToken: 'bad' }]) {
    await assertFails(save(db, extra));
  }
});
test('tombstones cannot be deleted or resurrected', async () => {
  const db = env.authenticatedContext('alice').firestore();
  await assertSucceeds(save(db));
  await assertSucceeds(save(db, { status: 'discarded', title: '', text: '', writeId: 'delete' }, 2));
  await assertFails(deleteDoc(doc(db, paymentPath)));
  await assertFails(save(db, { status: 'confirmed', writeId: 'resurrect' }, 3));
});
test('immutable identity and server timestamp are validated', async () => {
  const db = env.authenticatedContext('alice').firestore();
  await assertSucceeds(save(db));
  await assertFails(save(db, { id: 'changed' }, 2));
  await assertFails(save(db, { postedAt: 456 }, 2));
  await assertFails(save(db, { updatedAt: new Date(0) }, 2));
  await assertFails(setDoc(doc(db, headPath), metadata(22)));
  await assertFails(deleteDoc(doc(db, headPath)));
});
test('profile owner rules still work and remain isolated', async () => {
  const db = env.authenticatedContext('alice', { email: 'alice@example.com' }).firestore();
  await assertSucceeds(setDoc(doc(db, 'users/alice'), {
    userID: 'alice', name: 'Alice', email: 'alice@example.com', createdAt: serverTimestamp(),
  }));
  await assertFails(getDoc(doc(env.authenticatedContext('bob').firestore(), 'users/alice')));
});
