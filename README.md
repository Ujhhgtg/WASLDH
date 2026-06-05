# WASLDH — WAuxiliary String Literals Decryption Helper

Minimal Android app that exposes the WAuxiliary native decryption library
(`libwauxv-core.so`) as a local REST API, so the Python deobfuscation script
can decrypt obfuscated string literals without needing to reverse-engineer the
native algorithm.

## How it works

WAuxiliary replaces every string literal with `MagicFactory.get(long, String[])`,
which calls into `libwauxv-core.so` via JNI. Static analysis of the native library
is impractical (heavy obfuscation). WASLDH packages the exact same native lib and
string array into a bare-bones Android app that runs a Ktor HTTP server.

The deobfuscation script (`decrypt_encrypted_strings.py`) sends the long values
to this server and gets the decrypted strings back.

## API

### Single decrypt

```
GET /decrypt?key=<long>
```

Response: plain text of the decrypted string.

### Batch decrypt

```
GET /decryptBatch?keys=<long1>,<long2>,<long3>,...
```

Response: one line per key in `long:result` format. Newlines inside result
strings are escaped as `\n` / `\r`.

## Building & Running

```bash
# Build
./gradlew installDebug

# The app installs on the device as "WASLDH".
# Launch it, tap "Start Server" — the server listens on port 8080.

# Test
curl 'http://<device-ip>:8080/decrypt?key=4928230575635957130'
# → me.hd.wauxv
```

## Dependencies

- Android 15 (API 36) minimum
- [Ktor](https://ktor.io/) server with CIO engine
- Jetpack Compose UI
- `libwauxv-core.so` from WAuxiliary (bundled in `src/main/jniLibs/arm64-v8a/`)
