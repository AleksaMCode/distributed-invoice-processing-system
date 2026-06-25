from __future__ import annotations

import time
from datetime import datetime

from faker import Faker
from invoice.invoice import generate_one
from util.settings import load_settings


def run() -> None:
    settings = load_settings()
    faker = Faker(settings.faker_locale)
    settings.output_dir.mkdir(parents=True, exist_ok=True)

    print(f"Generating invoices into: {settings.output_dir.resolve()}")
    print("Press Ctrl+C to stop.")

    try:
        while True:
            file_name, xml = generate_one(settings, faker)
            output_path = settings.output_dir / file_name
            output_path.write_bytes(xml)
            print(f"[{datetime.now().isoformat()}] generated {output_path.name}")
            time.sleep(settings.sleep_seconds)
    except KeyboardInterrupt:
        print("Generator stopped by user.")


if __name__ == "__main__":
    run()
