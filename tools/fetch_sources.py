#!/usr/bin/env python3
"""Pull structured, primary-source opportunity data (no logins, no scraping of gated sites).

Usage: python3 tools/fetch_sources.py [--out opportunities/raw.json] [--companies tools/companies.txt]
Sources: Devpost API (online hackathons), SimplifyJobs Summer2027 list (India/Remote rows),
Greenhouse + Lever public job-board APIs for each company slug in companies.txt.
"""
import argparse, json, re, sys, urllib.request, urllib.parse
from concurrent.futures import ThreadPoolExecutor

UA = {"User-Agent": "opportunity-hunter/1.0"}
INTERN = re.compile(r"intern|trainee|apprentice|graduate|campus|new grad|early career", re.I)
INDIA = re.compile(r"india|bangalore|bengaluru|hyderabad|pune|gurgaon|gurugram|noida|mumbai|chennai|delhi|remote", re.I)
TECH = re.compile(r"software|backend|back-end|machine learning|\bml\b|\bai\b|data|sde|engineer|llm|research", re.I)

def get(url, timeout=25):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=timeout) as r:
        return r.read().decode("utf-8", "replace")

def devpost():
    out = []
    for page in (1, 2, 3):
        q = "https://devpost.com/api/hackathons?" + urllib.parse.urlencode(
            [("challenge_type[]", "online"), ("status[]", "open"), ("status[]", "upcoming"),
             ("order_by", "deadline"), ("page", page)])
        try:
            data = json.loads(get(q))
        except Exception as e:
            print("devpost error:", e, file=sys.stderr); break
        for h in data.get("hackathons", []):
            out.append({"source": "devpost", "type": "hackathon", "title": h.get("title"),
                        "org": h.get("organization_name"), "url": h.get("url"),
                        "location": (h.get("displayed_location") or {}).get("location"),
                        "dates": h.get("submission_period_dates"), "time_left": h.get("time_left_to_submission"),
                        "prize": re.sub("<[^>]+>", "", h.get("prize_amount") or ""),
                        "themes": [t.get("name") for t in h.get("themes", [])],
                        "verified_primary": True})
    return out

def simplify():
    out = []
    try:
        md = get("https://raw.githubusercontent.com/SimplifyJobs/Summer2027-Internships/dev/README.md", 60)
    except Exception as e:
        print("simplify error:", e, file=sys.stderr); return out
    for tr in re.findall(r"<tr>(.*?)</tr>", md, re.S):
        tds = re.findall(r"<td[^>]*>(.*?)</td>", tr, re.S)
        if len(tds) < 4 or "\U0001f512" in tr:
            continue
        loc = re.sub(r"<[^>]+>", " ", tds[2]).strip()
        india_hit = re.search(r"\b(india|bangalore|bengaluru|hyderabad|pune|gurgaon|gurugram|noida|mumbai|chennai|delhi)\b", loc, re.I)
        open_remote = re.search(r"\bremote\b", loc, re.I) and not re.search(r"\b(usa|us|uk|canada|europe|eu|germany|ireland)\b", loc, re.I)
        if not (india_hit or open_remote):
            continue
        link = re.search(r'href="([^"]+)"', tds[3])
        out.append({"source": "simplify-github", "type": "internship",
                    "org": re.sub(r"<[^>]+>", "", tds[0]).strip(), "title": re.sub(r"<[^>]+>", "", tds[1]).strip(),
                    "location": loc[:120], "url": link.group(1).split("?")[0] if link else None,
                    "verified_primary": False})
    return out

def board(slug):
    res = []
    try:
        d = json.loads(get(f"https://boards-api.greenhouse.io/v1/boards/{slug}/jobs"))
        for j in d.get("jobs", []):
            loc = (j.get("location") or {}).get("name", "")
            if INTERN.search(j["title"]) and TECH.search(j["title"]) and INDIA.search(loc):
                res.append({"source": f"greenhouse:{slug}", "type": "internship", "org": slug, "title": j["title"],
                            "location": loc, "url": j["absolute_url"], "updated": j.get("updated_at"), "verified_primary": True})
    except Exception:
        pass
    try:
        d = json.loads(get(f"https://api.lever.co/v0/postings/{slug}?mode=json"))
        for j in d:
            loc = (j.get("categories") or {}).get("location", "") or ""
            if INTERN.search(j["text"]) and TECH.search(j["text"]) and INDIA.search(loc):
                res.append({"source": f"lever:{slug}", "type": "internship", "org": slug, "title": j["text"],
                            "location": loc, "url": j["hostedUrl"], "verified_primary": True})
    except Exception:
        pass
    return res

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="opportunities/raw.json")
    ap.add_argument("--companies", default="tools/companies.txt")
    a = ap.parse_args()
    slugs = [l.strip() for l in open(a.companies) if l.strip() and not l.startswith("#")]
    results = devpost() + simplify()
    with ThreadPoolExecutor(12) as ex:
        for r in ex.map(board, slugs):
            results += r
    import os; os.makedirs(os.path.dirname(a.out) or ".", exist_ok=True)
    json.dump(results, open(a.out, "w"), indent=1)
    by = {}
    for r in results: by[r["source"].split(":")[0]] = by.get(r["source"].split(":")[0], 0) + 1
    print(f"wrote {len(results)} items to {a.out}: {by}")

if __name__ == "__main__":
    main()
