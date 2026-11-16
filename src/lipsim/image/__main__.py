"""Entry point: python -m lipsim.image ... (implementation in lipsim.image.run so that Windows
'spawn' worker processes can import the worker functions)."""
from lipsim.image.run import main

if __name__ == "__main__":
    main()
