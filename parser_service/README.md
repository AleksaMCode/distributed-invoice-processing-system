# Parser Service

Socket server that receives invoice frames from `watcher_service`, validates
integrity, deduplicates invoice IDs in Redis, calls validator via RMI, and
publishes results to RabbitMQ.

## Frame format (from watcher)

- 4 bytes: payload length (big-endian)
- N bytes: XML payload
- 32 bytes: SHA-256(payload)
- 4 bytes: filename length (big-endian)
- M bytes: filename UTF-8

## Flow

1. Read frame over TCP
2. Send ACK (`ACK|{filename}`)
3. Verify SHA-256 integrity
4. Parse XML and extract invoice
5. Redis dedupe (`SET key value NX EX 3600`)
6. Call validator via RMI
7. Publish to RabbitMQ:
   - valid -> `invoices.validated`
   - invalid -> `invoices.rejected`

## Configuration

Copy `.env.example` to `.env`.

## Run

```bash
./gradlew run
```
