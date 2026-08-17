import json, sqlite3, os

with open("data/quran/quran.json", encoding="utf-8") as f:
    data = json.load(f)

os.makedirs("data/quran", exist_ok=True)
conn = sqlite3.connect("data/quran/quran.db")
c = conn.cursor()

c.execute("""CREATE TABLE surahs (
    id INTEGER PRIMARY KEY,
    name_ar TEXT, name_en TEXT,
    revelation_place TEXT, ayah_count INTEGER
)""")

c.execute("""CREATE TABLE ayahs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    surah_id INTEGER, ayah_number INTEGER,
    arabic_text TEXT,
    FOREIGN KEY(surah_id) REFERENCES surahs(id)
)""")

for surah in data:
    c.execute("INSERT INTO surahs VALUES (?,?,?,?,?)", (
        surah["id"], surah["name"], surah["transliteration"],
        surah["type"], surah["total_verses"]
    ))
    for verse in surah["verses"]:
        c.execute("INSERT INTO ayahs (surah_id, ayah_number, arabic_text) VALUES (?,?,?)",
                   (surah["id"], verse["id"], verse["text"]))

conn.commit()

c.execute("SELECT COUNT(*) FROM surahs")
print("Surahs:", c.fetchone()[0])
c.execute("SELECT COUNT(*) FROM ayahs")
print("Ayahs:", c.fetchone()[0])

conn.close()
