"""Entry point: python -m lipsim.image ... (implementation in lipsim.image.run so that Windows
'spawn' worker processes can import the worker functions)."""
from lipsim.image.run import main
from lipsim.image.dispatch import dispatch

if __name__ == "__main__":
    delegated = dispatch()
    if delegated is None:
        main()
    else:
        raise SystemExit(delegated)
