#!/usr/bin/env python3
"""Pull structured, primary-source opportunity data (no logins, no scraping of gated sites).

Usage: python3 tools/fetch_sources.py [--out opportunities/raw.json] [--companies tools/companies.txt]
Sources: Devpost, Unstop, Codeforces, CodeChef, Internshala (public pages/APIs), SimplifyJobs list (India/Remote rows),
Greenhouse + Lever public job-board APIs for each company slug in companies.txt.
"""
import argparse, json, re, sys, urllib.request, urllib.parse
from concurrent.futures import ThreadPoolExecutor

UA = {"User-Agent": "opportunity-hunter/1.0"}
INTERN = re.compile(r"intern|trainee|apprentice|graduate|campus|new grad|early career", re.I)
INDIA = re.compile(r"india|bangalore|bengaluru|hyderabad|pune|gurgaon|gurugram|noida|mumbai|chennai|delhi|remote", re.I)
NONTECH = re.compile(r"marketing|sales|content|design|hr\b|human resource|business development|ambassador|video|social media|graphic|finance|accounting|telecall|recruit|writing|seo\b|customer|operations", re.I)
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


import datetime as dt, html as _html, time

NOW = dt.datetime.now(dt.timezone.utc)

def _iso(s):
    try:
        return dt.datetime.fromisoformat(s.replace("Z", "+00:00"))
    except Exception:
        return None

def codeforces(days):
    out = []
    try:
        for c in json.loads(get("https://codeforces.com/api/contest.list?gym=false"))["result"]:
            if c.get("phase") != "BEFORE":
                continue
            st = dt.datetime.fromtimestamp(c["startTimeSeconds"], dt.timezone.utc)
            if st - NOW <= dt.timedelta(days=days):
                out.append({"source": "codeforces", "type": "contest", "title": c["name"], "org": "Codeforces",
                            "location": "Online", "dates": st.strftime("%Y-%m-%d %H:%M UTC") + f" ({c['durationSeconds']//60} min)",
                            "url": f"https://codeforces.com/contest/{c['id']}", "verified_primary": True})
    except Exception as e:
        print("codeforces error:", e, file=sys.stderr)
    return out

def codechef(days):
    out = []
    try:
        d = json.loads(get("https://www.codechef.com/api/list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all"))
        for c in d.get("present_contests", []) + d.get("future_contests", []):
            st = _iso(c.get("contest_start_date_iso", ""))
            if st and st - NOW <= dt.timedelta(days=days):
                out.append({"source": "codechef", "type": "contest", "title": c["contest_name"], "org": "CodeChef",
                            "location": "Online", "dates": c["contest_start_date"] + f" ({c['contest_duration']} min)",
                            "url": "https://www.codechef.com/" + c["contest_code"], "verified_primary": True})
    except Exception as e:
        print("codechef error:", e, file=sys.stderr)
    return out

def unstop(days):
    out = []
    for kind in ("hackathons", "competitions", "internships"):
        for page in (1, 2):
            try:
                d = json.loads(get(f"https://unstop.com/api/public/opportunity/search-result?opportunity={kind}"
                                   f"&per_page=30&page={page}&oppstatus=open"))["data"]["data"]
            except Exception as e:
                print("unstop error:", kind, e, file=sys.stderr); break
            for o in d:
                end = _iso(o.get("end_date") or "")
                if end and end < NOW:
                    continue
                if end and end - NOW > dt.timedelta(days=max(days, 90)):
                    continue
                title = o.get("title", "")
                if kind == "internships" and not TECH.search(title + " " + " ".join(t.get("name", "") for t in o.get("required_skills", []) if isinstance(t, dict))):
                    continue
                org = (o.get("organisation") or {}).get("name")
                out.append({"source": "unstop", "type": kind.rstrip("s") if kind != "hackathons" else "hackathon",
                            "title": title, "org": org, "location": o.get("region") or "",
                            "dates": f"closes {o.get('end_date', '?')[:10]}",
                            "url": o.get("seo_url") or "https://unstop.com/" + o.get("public_url", ""),
                            "verified_primary": True})
    return out

INTERNSHALA_PAGES = ["python-django", "machine-learning", "artificial-intelligence", "backend-development",
                     "computer-science", "data-science", "software-development"]

def internshala():
    out, seen = [], set()
    for cat in INTERNSHALA_PAGES:
        try:
            h = get(f"https://internshala.com/internships/{cat}-internship/")
        except Exception as e:
            print("internshala error:", cat, e, file=sys.stderr); continue
        for blk in h.split('class="container-fluid individual_internship')[1:]:
            href = re.search(r"data-href='([^']+)'", blk)
            title = re.search(r'id="job_title"[^>]*>\s*([^<]+?)\s*</a>', blk)
            comp = re.search(r'class="company-name">\s*([^<]+?)\s*</p>', blk)
            loc = re.search(r'class="locations">.*?<span>(.*?)</span>', blk, re.S)
            stip = re.search(r"class='stipend'>([^<]+)<", blk)
            if not (href and title) or href.group(1) in seen:
                continue
            seen.add(href.group(1))
            out.append({"source": "internshala", "type": "internship", "title": _html.unescape(title.group(1)),
                        "org": _html.unescape(comp.group(1)) if comp else "",
                        "location": re.sub(r"<[^>]+>|\s+", " ", loc.group(1)).strip() if loc else "",
                        "stipend": _html.unescape(stip.group(1)) if stip else "",
                        "url": "https://internshala.com" + href.group(1), "verified_primary": True})
        time.sleep(1)  # be polite
    return out

def summary_md(results):
    order = {"internship": 0, "hackathon": 1, "contest": 2, "competition": 3}
    lines = ["# Raw opportunities (auto-generated; verify before applying)\n"]
    for typ in sorted({r["type"] for r in results}, key=lambda t: order.get(t, 9)):
        rows = [r for r in results if r["type"] == typ]
        lines += [f"\n## {typ.title()}s ({len(rows)})\n", "| Title | Org | Where | When/Stipend | Source | Link |", "|---|---|---|---|---|---|"]
        for r in rows:
            when = r.get("dates") or r.get("time_left") or r.get("stipend") or ""
            lines.append(f"| {r['title'][:70]} | {r.get('org') or ''} | {(r.get('location') or '')[:30]} | {when} | {r['source']} | {r.get('url') or ''} |")
    return "\n".join(lines) + "\n"

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default="opportunities/raw.json")
    ap.add_argument("--companies", default="tools/companies.txt")
    ap.add_argument("--days", type=int, default=60, help="contest/hackathon look-ahead window")
    ap.add_argument("--keywords", default="", help="comma-separated; keep only items whose title/org contains one")
    a = ap.parse_args()
    slugs = [l.strip() for l in open(a.companies) if l.strip() and not l.startswith("#")]
    results = devpost() + simplify() + codeforces(a.days) + codechef(a.days) + unstop(a.days) + internshala()
    with ThreadPoolExecutor(12) as ex:
        for r in ex.map(board, slugs):
            results += r
    import os; os.makedirs(os.path.dirname(a.out) or ".", exist_ok=True)
    results = [r for r in results if r["type"] not in ("internship",) or not NONTECH.search(r["title"]) or TECH.search(r["title"]) and not NONTECH.search(r["title"])]
    if a.keywords:
        kw = [k.strip().lower() for k in a.keywords.split(",") if k.strip()]
        results = [r for r in results if any(k in f"{r['title']} {r.get('org') or ''}".lower() for k in kw)]
    json.dump(results, open(a.out, "w"), indent=1)
    open(a.out.replace(".json", ".md"), "w").write(summary_md(results))
    by = {}
    for r in results: by[r["source"].split(":")[0]] = by.get(r["source"].split(":")[0], 0) + 1
    print(f"wrote {len(results)} items to {a.out}: {by}")

if __name__ == "__main__":
    main()
