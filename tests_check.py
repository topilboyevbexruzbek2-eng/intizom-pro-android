from pathlib import Path
import re
base=Path(__file__).parent
js=(base/'app/src/main/assets/app.js').read_text()
assert 'function smartReport(' in js
assert 'renderMonitor()' in js
assert 'data-act="reminder-status"' in js
assert 'intizom-pro-4.2' in js  # preserve old localStorage key
assert all((base/f'app/src/main/assets/books/{i}/original.pdf').is_file() for i in range(400,405))
for i, count in [(400,48),(401,31),(402,63),(403,72),(404,20)]:
    assert len(list((base/f'app/src/main/assets/books/{i}').glob('[0-9]*.webp')))==count
java=(base/'app/src/main/java/uz/intizom/pro/ReminderScheduler.java').read_text()
assert 'setExactAndAllowWhileIdle' in java and 'ACTION_BOOT_COMPLETED' in (base/'app/src/main/java/uz/intizom/pro/BootReceiver.java').read_text()
print('OK: 5 PDFs, 234 rendered book pages, reminder logic, AI monitoring, state continuity')
