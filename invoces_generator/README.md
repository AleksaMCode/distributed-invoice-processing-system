# Invoice Generator Service

Invoice generator that continuously creates random invoice XML files and writes
them to the watcher inbox path.

## Configuration

Copy `.env.example` to `.env` and adjust values.

Important defaults:

- `OUTPUT_DIR=./watcher_service/invoices/inbox`
- `TIMEZONE=Europe/Paris`
- `SLEEP_SECONDS=1`
- `CURRENCIES=BAM,EUR,USD,CHF,GBP`
- `MIN_ITEMS=1`, `MAX_ITEMS=6`
- `MIN_QTY=1`, `MAX_QTY=20`
- `MIN_UNIT_PRICE=50.00`, `MAX_UNIT_PRICE=5000.00`

## Run

```bash
pip install -r requirements.txt
python main.py
```

Runs indefinitely until terminated (<kbd>Ctrl</kbd>+<kbd>C</kbd>).
