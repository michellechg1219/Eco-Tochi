# e2e/robot/serial_kw.py
from serial import Serial

_ser = None

def open_serial_port(port, baud, timeout="1s"):
    """Abre el puerto serie. timeout admite '1s' o segundos en float."""
    global _ser
    to = 1.0
    if isinstance(timeout, str) and timeout.endswith("s"):
        try:
            to = float(timeout[:-1])
        except Exception:
            pass
    _ser = Serial(port, baudrate=int(baud), timeout=to)

def write_data(message):
    """Escribe texto en el puerto (UTF-8)."""
    if not _ser or not _ser.is_open:
        raise RuntimeError("Serial no está abierto")
    _ser.write(message.encode("utf-8"))

def close_serial_port():
    """Cierra el puerto si está abierto."""
    global _ser
    if _ser and _ser.is_open:
        _ser.close()
