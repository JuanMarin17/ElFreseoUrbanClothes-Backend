import time
import logging

logger = logging.getLogger(__name__)


class CircuitBreaker:
    """
    Circuit breaker para llamadas a servicios downstream en asyncio.
    Seguro sin locks porque asyncio es single-threaded (no hay preempción).
    """

    CLOSED    = "CLOSED"
    OPEN      = "OPEN"
    HALF_OPEN = "HALF_OPEN"

    def __init__(self, name: str, failure_threshold: int = 3, reset_timeout: int = 30):
        self.name = name
        self.failure_threshold = failure_threshold
        self.reset_timeout = reset_timeout
        self._failures = 0
        self._state = self.CLOSED
        self._opened_at: float = 0.0

    async def call(self, coro):
        """
        Ejecuta la corutina si el circuito está cerrado.
        Retorna None cuando el circuito está abierto (sin hacer la llamada).
        Captura excepciones y las cuenta como fallos — retorna None en ese caso también.
        """
        if self._state == self.OPEN:
            if time.monotonic() - self._opened_at >= self.reset_timeout:
                self._state = self.HALF_OPEN
                logger.info("[CB:%s] HALF_OPEN — probando conexión", self.name)
            else:
                coro.close()  # evita "coroutine never awaited" warning
                logger.debug("[CB:%s] OPEN — llamada descartada", self.name)
                return None

        try:
            result = await coro
            if self._state == self.HALF_OPEN:
                self._state = self.CLOSED
                self._failures = 0
                logger.info("[CB:%s] CLOSED — servicio recuperado", self.name)
            return result
        except Exception as exc:
            self._failures += 1
            if self._failures >= self.failure_threshold:
                self._state = self.OPEN
                self._opened_at = time.monotonic()
                logger.warning(
                    "[CB:%s] OPEN tras %d fallos consecutivos. Último error: %s",
                    self.name, self._failures, exc
                )
            return None

    @property
    def state(self) -> str:
        return self._state
