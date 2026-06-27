import re
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from pathlib import Path
from unittest import TestCase
from unittest.mock import patch
from xml.etree import ElementTree as ET
from zoneinfo import ZoneInfo

from invoice.invoice import generate_one


@dataclass
class _Settings:
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


class _FakeFaker:
    def company(self) -> str:
        return "DIPS"

    def company_email(self) -> str:
        return "invoice@dips.com"


class TestGenerateOne(TestCase):
    allowed_currencies = {"BAM", "EUR", "USD", "CHF", "GBP"}

    def setUp(self) -> None:
        self.settings = _Settings(
            output_dir=Path("."),
            timezone="Europe/Paris",
            sleep_seconds=1.0,
            currencies=["BAM", "EUR", "USD", "CHF", "GBP"],
            min_items=1,
            max_items=1,
            min_qty=1,
            max_qty=1,
            min_unit_price=Decimal("100.00"),
            max_unit_price=Decimal("100.00"),
            faker_locale="en_US",
        )
        self.faker = _FakeFaker()
        self._tz_marker = object()
        self.zoneinfo_patcher = patch(
            "invoice.invoice.ZoneInfo", return_value=self._tz_marker
        )
        self.datetime_patcher = patch("invoice.invoice.datetime")
        self.mock_zoneinfo = self.zoneinfo_patcher.start()
        self.mock_datetime = self.datetime_patcher.start()
        self.mock_datetime.now.return_value = datetime(2027, 3, 4, 10, 30)

    def tearDown(self) -> None:
        self.zoneinfo_patcher.stop()
        self.datetime_patcher.stop()

    def test_generated_id_matches_inv_year_uuid4_format(self) -> None:
        _, xml_bytes = generate_one(self.settings, self.faker)
        root = ET.fromstring(xml_bytes)
        invoice_id = root.findtext("id")

        self.assertIsNotNone(invoice_id)
        self.assertRegex(
            invoice_id,
            r"^INV-\d{4}-[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
        )

    def test_generated_ein_is_13_digits(self) -> None:
        _, xml_bytes = generate_one(self.settings, self.faker)
        root = ET.fromstring(xml_bytes)
        ein = root.findtext("client/ein")

        self.assertIsNotNone(ein)
        self.assertRegex(ein, r"^\d{13}$")

    def test_currency_is_one_of_allowed_currencies(self) -> None:
        _, xml_bytes = generate_one(self.settings, self.faker)
        root = ET.fromstring(xml_bytes)

        self.assertIn(root.findtext("currency"), self.allowed_currencies)

    def test_xml_contains_required_nodes(self) -> None:
        _, xml_bytes = generate_one(self.settings, self.faker)
        root = ET.fromstring(xml_bytes)

        self.assertEqual("invoice", root.tag)
        self.assertIsNotNone(root.find("id"))
        self.assertIsNotNone(root.find("client/ein"))
        self.assertIsNotNone(root.find("date"))
        self.assertIsNotNone(root.find("currency"))
        self.assertIsNotNone(root.find("items"))
        self.assertGreater(len(root.findall("items/item")), 0)

    def test_timezone_and_date_use_europe_paris(self) -> None:
        _, xml_bytes = generate_one(self.settings, self.faker)
        root = ET.fromstring(xml_bytes)

        self.mock_zoneinfo.assert_called_with("Europe/Paris")
        self.mock_datetime.now.assert_called_with(self._tz_marker)
        self.assertEqual("2027-03-04", root.findtext("date"))
        self.assertTrue(root.findtext("id").startswith("INV-2027-"))
