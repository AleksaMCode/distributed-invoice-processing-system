from __future__ import annotations

import random
import uuid
from datetime import datetime
from decimal import ROUND_HALF_UP, Decimal
from typing import TYPE_CHECKING
from xml.etree.ElementTree import Element, SubElement, tostring
from zoneinfo import ZoneInfo

if TYPE_CHECKING:
    from faker import Faker
    from util.settings import Settings

DEFAULT_ITEMS = [
    "Frontend development",
    "Backend API implementation",
    "Mobile app feature delivery",
    "Cloud infrastructure setup",
    "CI/CD pipeline automation",
    "QA automation suite",
    "UI/UX design system update",
    "Security hardening and audit",
    "Performance optimization",
    "Production incident support",
    "Data analytics dashboard",
    "Technical architecture consulting",
]


def _get_price(value: Decimal) -> str:
    return str(value.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))


def _get_random_price(min_price: Decimal, max_price: Decimal) -> Decimal:
    sampled = Decimal(str(random.uniform(float(min_price), float(max_price))))
    return sampled.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def build_invoice_xml(
    invoice_id: str,
    client_name: str,
    ein: str,
    client_email: str,
    invoice_date: str,
    items: list[tuple[str, int, Decimal]],
) -> bytes:
    root = Element("invoice")
    SubElement(root, "id").text = invoice_id

    client = SubElement(root, "client")
    SubElement(client, "name").text = client_name
    SubElement(client, "ein").text = ein
    SubElement(client, "email").text = client_email

    SubElement(root, "date").text = invoice_date
    SubElement(root, "currency").text = "EUR"

    items_node = SubElement(root, "items")
    for description, quantity, unit_price in items:
        item_node = SubElement(items_node, "item")
        SubElement(item_node, "description").text = description
        SubElement(item_node, "quantity").text = str(quantity)
        SubElement(item_node, "unitPrice").text = _get_price(unit_price)

    xml_body = tostring(root, encoding="utf-8")
    return b'<?xml version="1.0" encoding="UTF-8"?>\n' + xml_body + b"\n"


def generate_one(settings: Settings, faker: Faker) -> tuple[str, bytes]:
    now = datetime.now(ZoneInfo(settings.timezone))
    invoice_id = f"INV-{now.year}-{uuid.uuid4()}"
    file_name = f"invoice-{uuid.uuid4()}.xml"

    client_name = faker.company()
    ein = "".join(random.choices("0123456789", k=9))
    client_email = faker.company_email()

    item_count = random.randint(settings.min_items, settings.max_items)
    item_descriptions = random.choices(DEFAULT_ITEMS, k=item_count)

    items: list[tuple[str, int, Decimal]] = []
    for description in item_descriptions:
        quantity = random.randint(settings.min_qty, settings.max_qty)
        unit_price = _get_random_price(settings.min_unit_price, settings.max_unit_price)
        items.append((description, quantity, unit_price))

    xml_bytes = build_invoice_xml(
        invoice_id=invoice_id,
        client_name=client_name,
        ein=ein,
        client_email=client_email,
        invoice_date=now.date().isoformat(),
        items=items,
    )
    return file_name, xml_bytes
