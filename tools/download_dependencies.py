"""Download upstream release dependencies, without installing software globally."""
import hashlib
import urllib.request
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
FILES = {
 "BigReactors-0.4.3A.jar": "https://github.com/erogenousbeef-zz/BigReactors/releases/download/0.4.3A/BigReactors-0.4.3A.jar",
 "CoFHCore-1.7.10-3.1.4-329.jar": "https://edge.forgecdn.net/files/2388/750/CoFHCore-%5B1.7.10%5D3.1.4-329.jar",
}
for name, url in FILES.items():
 target = ROOT / "libs" / name
 target.parent.mkdir(exist_ok=True)
 data = target.read_bytes() if target.exists() else urllib.request.urlopen(url, timeout=90).read()
 target.write_bytes(data)
 print(name, hashlib.sha256(data).hexdigest())

plugin = ROOT / "tools" / "retrofuturagradle-1.4.1.jar"
if not plugin.exists():
 plugin.write_bytes(urllib.request.urlopen("https://github.com/GTNewHorizons/RetroFuturaGradle/releases/download/1.4.1/retrofuturagradle-1.4.1.jar", timeout=120).read())
print(plugin.name, hashlib.sha256(plugin.read_bytes()).hexdigest())
