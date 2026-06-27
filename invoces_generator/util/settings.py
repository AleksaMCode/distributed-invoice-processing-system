from __future__ import annotations

import os
from dataclasses import dataclass
from decimal import Decimal
from pathlib import Path

from dotenv import load_dotenv


@dataclass(frozen=True)
class Settings:
    output_dir: Path
    timezone: str
    sleep_seconds: float
    currencies: list[str]
    min_items: int
    max_items: int
    min_qty: int
    max_qty: int
    min_unit_price: Decimal
    max_unit_price: Decimal
    faker_locale: str


SERVICE_DIR = Path(__file__).resolve().parents[1]
REPO_ROOT = SERVICE_DIR.parent


def _env(name: str, default: str) -> str:
    value = os.getenv(name)
    return value.strip() if value and value.strip() else default


def load_settings() -> Settings:
    load_dotenv(dotenv_path=SERVICE_DIR / ".env")

    output_dir = Path(_env("OUTPUT_DIR", "./watcher_service/invoices/inbox"))
    if not output_dir.is_absolute():
        output_dir = (REPO_ROOT / output_dir).resolve()

    raw_currencies = _env("CURRENCIES", "BAM,EUR,USD,CHF,GBP")
    currencies = [
        part.strip().upper() for part in raw_currencies.split(",") if part.strip()
    ]

    settings = Settings(
        output_dir=output_dir,
        timezone=_env("TIMEZONE", "Europe/Paris"),
        sleep_seconds=float(_env("SLEEP_SECONDS", "1")),
        currencies=currencies,
        min_items=int(_env("MIN_ITEMS", "1")),
        max_items=int(_env("MAX_ITEMS", "6")),
        min_qty=int(_env("MIN_QTY", "1")),
        max_qty=int(_env("MAX_QTY", "20")),
        min_unit_price=Decimal(_env("MIN_UNIT_PRICE", "50.00")),
        max_unit_price=Decimal(_env("MAX_UNIT_PRICE", "5000.00")),
        faker_locale=_env("FAKER_LOCALE", "en_US"),
    )

    if settings.min_items < 1 or settings.max_items < settings.min_items:
        raise ValueError("Invalid item range configuration")
    if settings.min_qty < 1 or settings.max_qty < settings.min_qty:
        raise ValueError("Invalid quantity range configuration")
    if (
        settings.min_unit_price <= 0
        or settings.max_unit_price < settings.min_unit_price
    ):
        raise ValueError("Invalid unit price range configuration")
    if settings.sleep_seconds < 0:
        raise ValueError("SLEEP_SECONDS must be >= 0")
    if not settings.currencies:
        raise ValueError("CURRENCIES must contain at least one currency")

    return settings
