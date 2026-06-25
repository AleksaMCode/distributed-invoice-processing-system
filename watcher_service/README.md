# Watcher Service

## Behavior

- Monitors `INBOX_DIR` for new `*.xml` files.
- On startup, scans existing XML files in inbox and queues them as new.
- Uses an internal bounded queue (`QUEUE_CAPACITY`) for backpressure.
- Single processor thread consumes files one by one.
- Processing flow:
  1. Read file bytes.
  2. Build frame: `4-byte big-endian xml length + payload + 32-byte SHA-256 + 4-byte filename length + filename bytes`.
  3. Send over TCP to parser.
  4. Wait for ACK.
  5. On success, move to processed with UUID suffix.
  6. On no/invalid ACK or socket error, move to failed with UUID suffix.

## ACK format expected by watcher

- First `3` bytes must be status: `ACK`.
- Remaining bytes are interpreted as the filename (UTF-8), with optional delimiter characters before the name.
- Example: `ACK|invoice-001.xml`.

## Config

Copy `.env.example` to `.env` and adjust values.

- `INBOX_DIR`
- `PROCESSED_DIR`
- `FAILED_DIR`
- `PARSER_HOST`
- `PARSER_PORT`
- `QUEUE_CAPACITY`
- `SOCKET_CONNECT_TIMEOUT_MS`
- `SOCKET_READ_TIMEOUT_MS`
- `FILE_STABILITY_CHECKS`
- `FILE_STABILITY_DELAY_MS`
- `WATCH_POLL_TIMEOUT_MS`
