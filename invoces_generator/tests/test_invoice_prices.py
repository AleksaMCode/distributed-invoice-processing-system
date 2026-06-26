from decimal import Decimal
from unittest import TestCase
from unittest.mock import patch

from invoice.invoice import _get_price, _get_random_price


class TestInvoicePriceHelpers(TestCase):
    def test_get_price_rounds_half_up(self) -> None:
        self.assertEqual(_get_price(Decimal("12.345")), "12.35")
        self.assertEqual(_get_price(Decimal("12.344")), "12.34")

    def test_get_price_keeps_two_decimals(self) -> None:
        self.assertEqual(_get_price(Decimal("10")), "10.00")
        self.assertEqual(_get_price(Decimal("0.1")), "0.10")

    @patch("invoice.invoice.random.uniform", return_value=123.456)
    def test_get_random_price_quantizes_to_two_decimals(self, _mock_uniform) -> None:
        result = _get_random_price(Decimal("100.00"), Decimal("200.00"))
        self.assertEqual(result, Decimal("123.46"))

    @patch("invoice.invoice.random.uniform", return_value=200.0)
    def test_get_random_price_respects_range_limits(self, _mock_uniform) -> None:
        result = _get_random_price(Decimal("100.00"), Decimal("200.00"))
        self.assertGreaterEqual(result, Decimal("100.00"))
        self.assertLessEqual(result, Decimal("200.00"))
