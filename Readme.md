<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Aegis — Family Safety Platform</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:ital,wght@0,300;0,400;0,500;1,300&family=DM+Mono:wght@400;500&display=swap');

  :root {
    --bg: #0a0c10;
    --bg2: #111318;
    --bg3: #181c24;
    --border: rgba(255,255,255,0.07);
    --border2: rgba(255,255,255,0.13);
    --text: #e8eaf0;
    --muted: #7a7f8e;
    --accent: #4ade80;
    --accent2: #22d3ee;
    --accent3: #f472b6;
    --warn: #fb923c;
    --red: #f87171;
    --purple: #a78bfa;
    --glow: rgba(74,222,128,0.12);
    --r: 10px;
  }

  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

  html { scroll-behavior: smooth; }

  body {
    background: var(--bg);
    color: var(--text);
    font-family: 'DM Sans', sans-serif;
    font-size: 15px;
    line-height: 1.75;
    min-height: 100vh;
  }

  /* Noise texture overlay */
  body::before {
    content: '';
    position: fixed;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E");
    pointer-events: none;
    z-index: 0;
    opacity: 0.5;
  }

  .container {
    max-width: 960px;
    margin: 0 auto;
    padding: 0 24px 80px;
    position: relative;
    z-index: 1;
  }

  /* ── HERO ── */
  .hero {
    padding: 72px 0 48px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 20px;
    position: relative;
  }

  .hero-glow {
    position: absolute;
    top: -40px;
    left: 50%;
    transform: translateX(-50%);
    width: 520px;
    height: 320px;
    background: radial-gradient(ellipse at center, rgba(74,222,128,0.08) 0%, transparent 70%);
    pointer-events: none;
  }

  .hero-shield {
    width: 72px;
    height: 72px;
    animation: pulse-shield 3s ease-in-out infinite;
  }

  @keyframes pulse-shield {
    0%, 100% { filter: drop-shadow(0 0 8px rgba(74,222,128,0.4)); }
    50% { filter: drop-shadow(0 0 20px rgba(74,222,128,0.8)); }
  }

  h1 {
    font-family: 'Syne', sans-serif;
    font-size: clamp(2.6rem, 6vw, 4rem);
    font-weight: 800;
    letter-spacing: -0.03em;
    line-height: 1.05;
    background: linear-gradient(135deg, #e8eaf0 30%, #4ade80 70%, #22d3ee 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .hero-tagline {
    font-size: 1.05rem;
    color: var(--muted);
    max-width: 520px;
    font-weight: 300;
    letter-spacing: 0.01em;
  }

  /* ── BADGES ── */
  .badges {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
  }

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 12px;
    border-radius: 999px;
    font-family: 'DM Mono', monospace;
    font-size: 11px;
    font-weight: 500;
    border: 1px solid;
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }

  .badge.green  { background: rgba(74,222,128,0.08);  border-color: rgba(74,222,128,0.3);  color: #4ade80; }
  .badge.purple { background: rgba(167,139,250,0.08); border-color: rgba(167,139,250,0.3); color: #a78bfa; }
  .badge.blue   { background: rgba(34,211,238,0.08);  border-color: rgba(34,211,238,0.3);  color: #22d3ee; }
  .badge.orange { background: rgba(251,146,60,0.08);  border-color: rgba(251,146,60,0.3);  color: #fb923c; }
  .badge.pink   { background: rgba(244,114,182,0.08); border-color: rgba(244,114,182,0.3); color: #f472b6; }
  .badge.gray   { background: rgba(255,255,255,0.04); border-color: rgba(255,255,255,0.12); color: var(--muted); }

  /* ── DIVIDER ── */
  .divider {
    height: 1px;
    background: var(--border);
    margin: 40px 0;
  }

  /* ── SECTIONS ── */
  h2 {
    font-family: 'Syne', sans-serif;
    font-size: 1.5rem;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: var(--text);
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  h2 .icon { font-size: 1.1em; }

  h3 {
    font-family: 'Syne', sans-serif;
    font-size: 1.05rem;
    font-weight: 600;
    color: var(--text);
    margin: 28px 0 12px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  p { color: var(--muted); margin-bottom: 12px; line-height: 1.8; }
  p strong { color: var(--text); font-weight: 500; }

  /* ── OVERVIEW CARDS ── */
  .cards-2 {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
    margin: 24px 0;
  }

  .card {
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: var(--r);
    padding: 20px;
    transition: border-color 0.2s, background 0.2s;
  }

  .card:hover { border-color: var(--border2); background: var(--bg3); }

  .card-icon { font-size: 1.4rem; margin-bottom: 10px; }
  .card-title { font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 600; color: var(--text); margin-bottom: 6px; }
  .card-desc { font-size: 0.82rem; color: var(--muted); line-height: 1.6; }

  /* ── FEATURE TABLES ── */
  .feature-section { margin: 32px 0; }

  .feature-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1px;
    background: var(--border);
    border: 1px solid var(--border);
    border-radius: var(--r);
    overflow: hidden;
    margin-top: 12px;
  }

  .feature-item {
    background: var(--bg2);
    padding: 14px 18px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .feature-item:hover { background: var(--bg3); }

  .feature-name {
    font-family: 'DM Mono', monospace;
    font-size: 0.8rem;
    font-weight: 500;
    color: var(--accent);
  }

  .feature-desc { font-size: 0.82rem; color: var(--muted); line-height: 1.5; }

  /* ── TECH STACK ── */
  .tech-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 10px;
    margin-top: 16px;
  }

  .tech-item {
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 14px 16px;
    display: flex;
    flex-direction: column;
    gap: 3px;
    transition: border-color 0.2s;
  }

  .tech-item:hover { border-color: var(--border2); }

  .tech-cat {
    font-size: 0.7rem;
    color: var(--muted);
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-family: 'DM Mono', monospace;
  }

  .tech-name {
    font-family: 'Syne', sans-serif;
    font-size: 0.88rem;
    font-weight: 600;
    color: var(--text);
  }

  .tech-desc { font-size: 0.78rem; color: var(--muted); }

  /* ── DIAGRAMS ── */
  .diagram-card {
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: var(--r);
    padding: 28px 20px;
    margin: 16px 0;
    overflow-x: auto;
  }

  .diagram-title {
    font-family: 'DM Mono', monospace;
    font-size: 0.75rem;
    color: var(--muted);
    text-transform: uppercase;
    letter-spacing: 0.1em;
    margin-bottom: 20px;
  }

  /* ── FLOW DIAGRAM SVG STYLES ── */
  .flow-node rect { stroke-width: 1; }
  .flow-text { font-family: 'DM Sans', sans-serif; font-size: 12px; fill: #e8eaf0; }
  .flow-sub  { font-family: 'DM Sans', sans-serif; font-size: 10px; fill: #7a7f8e; }
  .flow-arr  { fill: none; stroke: rgba(255,255,255,0.2); stroke-width: 1.2; marker-end: url(#arr); }
  .flow-arr-g  { fill: none; stroke: rgba(74,222,128,0.5); stroke-width: 1.2; marker-end: url(#arr-g); }
  .flow-arr-r  { fill: none; stroke: rgba(248,113,113,0.5); stroke-width: 1.2; marker-end: url(#arr-r); }
  .flow-arr-b  { fill: none; stroke: rgba(34,211,238,0.5); stroke-width: 1.2; marker-end: url(#arr-b); }
  .flow-label { font-family: 'DM Mono', monospace; font-size: 9px; fill: #7a7f8e; }

  /* ── ARCHITECTURE LAYER DIAGRAM ── */
  .arch-layer { transition: opacity 0.2s; cursor: default; }

  /* ── CODE BLOCK ── */
  pre, code {
    font-family: 'DM Mono', monospace;
    font-size: 0.82rem;
  }

  pre {
    background: var(--bg3);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 18px 20px;
    overflow-x: auto;
    line-height: 1.65;
    color: #c8cdd8;
    margin: 12px 0;
  }

  .code-keyword { color: #a78bfa; }
  .code-string  { color: #4ade80; }
  .code-comment { color: #4a5060; }
  .code-path    { color: #22d3ee; }
  .code-file    { color: #fb923c; }

  /* ── ROADMAP ── */
  .roadmap { display: flex; flex-direction: column; gap: 0; margin: 16px 0; }

  .roadmap-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px 0;
    border-bottom: 1px solid var(--border);
    position: relative;
  }

  .roadmap-item:last-child { border-bottom: none; }

  .roadmap-dot {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: 1.5px solid;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.75rem;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .dot-done   { border-color: var(--accent);  color: var(--accent);  background: rgba(74,222,128,0.08); }
  .dot-active { border-color: var(--warn);    color: var(--warn);    background: rgba(251,146,60,0.08); }
  .dot-plan   { border-color: var(--border2); color: var(--muted);   background: transparent; }

  .roadmap-content { flex: 1; }
  .roadmap-phase { font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 600; color: var(--text); }
  .roadmap-features { font-size: 0.82rem; color: var(--muted); margin-top: 3px; }

  /* ── PERMISSIONS ── */
  .perms-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 10px;
    margin-top: 12px;
  }

  .perm-item {
    background: var(--bg2);
    border: 1px solid var(--border);
    border-radius: 8px;
    padding: 12px 16px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .perm-name {
    font-family: 'DM Mono', monospace;
    font-size: 0.76rem;
    color: var(--accent2);
  }

  .perm-desc { font-size: 0.8rem; color: var(--muted); }

  /* ── INSTALL STEPS ── */
  .steps { display: flex; flex-direction: column; gap: 16px; margin: 16px 0; }

  .step {
    display: flex;
    gap: 16px;
    align-items: flex-start;
  }

  .step-num {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: rgba(74,222,128,0.1);
    border: 1px solid rgba(74,222,128,0.3);
    color: var(--accent);
    font-family: 'Syne', sans-serif;
    font-size: 0.8rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .step-body { flex: 1; }
  .step-title { font-family: 'Syne', sans-serif; font-size: 0.9rem; font-weight: 600; color: var(--text); margin-bottom: 6px; }

  /* ── FOOTER ── */
  .footer {
    margin-top: 60px;
    padding-top: 32px;
    border-top: 1px solid var(--border);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    text-align: center;
  }

  .footer-logo {
    font-family: 'Syne', sans-serif;
    font-size: 1.2rem;
    font-weight: 800;
    background: linear-gradient(135deg, #4ade80, #22d3ee);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .footer-text { font-size: 0.82rem; color: var(--muted); }

  a { color: var(--accent2); text-decoration: none; }
  a:hover { text-decoration: underline; }

  /* ── PREREQ TABLE ── */
  .req-table {
    width: 100%;
    border-collapse: collapse;
    border: 1px solid var(--border);
    border-radius: var(--r);
    overflow: hidden;
    margin-top: 12px;
    font-size: 0.85rem;
  }

  .req-table thead tr { background: var(--bg3); }
  .req-table th { padding: 12px 16px; text-align: left; color: var(--muted); font-weight: 500; font-family: 'DM Mono', monospace; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.06em; border-bottom: 1px solid var(--border); }
  .req-table td { padding: 11px 16px; border-bottom: 1px solid var(--border); color: var(--muted); }
  .req-table td:first-child { color: var(--text); font-weight: 500; }
  .req-table tr:last-child td { border-bottom: none; }
  .req-table tbody tr { background: var(--bg2); }
  .req-table tbody tr:hover { background: var(--bg3); }

  td code { background: rgba(255,255,255,0.06); padding: 2px 6px; border-radius: 4px; font-size: 0.8rem; color: var(--accent2); }

  @media (max-width: 600px) {
    .feature-grid { grid-template-columns: 1fr; }
    .cards-2 { grid-template-columns: 1fr; }
  }
</style>
</head>
<body>
<div class="container">

  <!-- ══ HERO ══ -->
  <div class="hero">
    <div class="hero-glow"></div>

    <!-- Shield SVG inline -->
    <svg class="hero-shield" viewBox="0 0 72 72" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M36 6L10 18V38C10 52.4 21.6 65.8 36 69C50.4 65.8 62 52.4 62 38V18L36 6Z" fill="rgba(74,222,128,0.1)" stroke="#4ade80" stroke-width="1.5"/>
      <path d="M36 14L16 24V38C16 49.2 24.8 59.8 36 63C47.2 59.8 56 49.2 56 38V24L36 14Z" fill="rgba(74,222,128,0.06)"/>
      <path d="M26 36L32 42L46 28" stroke="#4ade80" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>

    <h1>Aegis</h1>
    <p class="hero-tagline">Real-time Family Safety &amp; Emergency Response Platform for Android</p>

    <div class="badges">
      <span class="badge green">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M17.05 2H6.95C4.5 2 2.5 4 2.5 6.45v11.1C2.5 20 4.5 22 6.95 22h10.1c2.45 0 4.45-2 4.45-4.45V6.45C21.5 4 19.5 2 17.05 2zm-4.55 16l-5-5 1.41-1.41 3.59 3.58 7.59-7.59 1.41 1.42-9 9z"/></svg>
        Android
      </span>
      <span class="badge purple">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M18.68 11.98c-.43-.5-.97-.98-1.58-1.43l-2.62-1.98c-.76-.57-1.13-1.2-1.13-1.87v-.08h-2.7v.08c0 1.03.46 2 1.38 2.7l2.62 1.98c.47.36.85.72 1.12 1.07.56.73.84 1.55.84 2.46C16.61 17.38 14.71 19 12 19s-4.61-1.62-4.61-4.07c0-.91.28-1.73.85-2.46.27-.35.65-.71 1.12-1.07l2.62-1.98c.92-.7 1.38-1.67 1.38-2.7v-.08H10.66v.08c0 .67-.37 1.3-1.13 1.87l-2.62 1.98c-.61.45-1.15.93-1.58 1.43C4.47 13.29 4 14.7 4 16.07 4 19.34 7.09 22 12 22s8-2.66 8-5.93c0-1.37-.47-2.78-1.32-4.09z"/></svg>
        Kotlin
      </span>
      <span class="badge blue">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
        Jetpack Compose
      </span>
      <span class="badge orange">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><path d="M3.89 15.67L1 18.56l1.41 1.41 2.9-2.89-1.42-1.41zM11 0H9v3h2V0zm8.11 1.03L17.7 2.45l2.12 2.12 1.41-1.42-2.12-2.12zM17 9c0-2.76-2.24-5-5-5-2.77 0-5 2.24-5 5 0 2.29 1.54 4.23 3.67 4.8L11 23h2l.33-9.2C15.46 13.23 17 11.29 17 9zM21 8h3v2h-3V8zM0 8h3v2H0V8zM2.45 3.45l2.12 2.12L3.16 6.98 1.03 4.86l1.42-1.41z"/></svg>
        Firebase
      </span>
      <span class="badge pink">MIT License</span>
      <span class="badge gray">minSdk 26 · targetSdk 34</span>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ OVERVIEW ══ -->
  <h2><span class="icon">📋</span> Overview</h2>
  <p>Aegis is a production-ready Android application built with modern development practices, providing families with round-the-clock protection. Built on <strong>Clean Architecture + MVVM</strong>, it handles everything from crash detection to geofencing with offline resilience.</p>

  <div class="cards-2">
    <div class="card">
      <div class="card-icon">🚨</div>
      <div class="card-title">Instant Emergency Response</div>
      <div class="card-desc">Multi-trigger SOS system with automatic crash &amp; fall detection, 30s cancellation window, and SMS fallback.</div>
    </div>
    <div class="card">
      <div class="card-icon">📍</div>
      <div class="card-title">Real-time Location Sharing</div>
      <div class="card-desc">Continuous GPS tracking with configurable intervals, offline queue support, and route deviation alerts.</div>
    </div>
    <div class="card">
      <div class="card-icon">🔵</div>
      <div class="card-title">Intelligent Geofencing</div>
      <div class="card-desc">Customizable safe zones with instant entry/exit notifications, synced across all family devices via Firebase.</div>
    </div>
    <div class="card">
      <div class="card-icon">👨‍👩‍👧</div>
      <div class="card-title">Family Coordination</div>
      <div class="card-desc">Group management with role-based permissions, real-time member status, and invite-link onboarding.</div>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ KEY FEATURES ══ -->
  <h2><span class="icon">✨</span> Key Features</h2>

  <div class="feature-section">
    <h3>🚨 Emergency Response</h3>
    <div class="feature-grid">
      <div class="feature-item"><span class="feature-name">SOS Alert</span><span class="feature-desc">One-tap emergency alert with live GPS location sent to all emergency contacts instantly.</span></div>
      <div class="feature-item"><span class="feature-name">Auto Crash Detection</span><span class="feature-desc">Accelerometer-based collision detection at 3.5G threshold with a 30-second cancellation countdown.</span></div>
      <div class="feature-item"><span class="feature-name">Fall Detection</span><span class="feature-desc">Freefall pattern recognition using accelerometer analysis for detecting sudden drops.</span></div>
      <div class="feature-item"><span class="feature-name">Fatigue Detection</span><span class="feature-desc">ML Kit-powered driver drowsiness detection via front camera for safe driving monitoring.</span></div>
      <div class="feature-item"><span class="feature-name">Shake SOS</span><span class="feature-desc">Rapidly shaking device 5× in 2 seconds triggers a silent emergency alert.</span></div>
      <div class="feature-item"><span class="feature-name">Volume Button SOS</span><span class="feature-desc">Triple volume press triggers a silent emergency without unlocking the screen.</span></div>
      <div class="feature-item"><span class="feature-name">Power Button SOS</span><span class="feature-desc">Five rapid power button presses activates the emergency alert discreetly.</span></div>
      <div class="feature-item"><span class="feature-name">Offline SMS Fallback</span><span class="feature-desc">Automatic SMS delivery of location when no internet connectivity is available.</span></div>
    </div>
  </div>

  <div class="feature-section">
    <h3>📍 Location Services</h3>
    <div class="feature-grid">
      <div class="feature-item"><span class="feature-name">Live Tracking</span><span class="feature-desc">Real-time GPS sharing — every 5s active mode (3m delta) / 30s passive mode (20m delta).</span></div>
      <div class="feature-item"><span class="feature-name">Route Deviation</span><span class="feature-desc">Alerts dispatched when user deviates 200m+ from any planned route.</span></div>
      <div class="feature-item"><span class="feature-name">Speed Monitoring</span><span class="feature-desc">Immediate alerts triggered for abnormal or dangerous speed detection.</span></div>
      <div class="feature-item"><span class="feature-name">Location History</span><span class="feature-desc">Full movement history stored locally and remotely for later review.</span></div>
      <div class="feature-item"><span class="feature-name">Offline Queue</span><span class="feature-desc">Room DB-based location caching during network outages; auto-flushed when back online.</span></div>
      <div class="feature-item"><span class="feature-name">ETA Tracking</span><span class="feature-desc">Estimated arrival time calculation shared with family members in real time.</span></div>
    </div>
  </div>

  <div class="feature-section">
    <h3>🔵 Geofencing</h3>
    <div class="feature-grid">
      <div class="feature-item"><span class="feature-name">Safe Zones</span><span class="feature-desc">Custom radius zones (default 150m) with instant entry/exit push notifications.</span></div>
      <div class="feature-item"><span class="feature-name">Danger Zones</span><span class="feature-desc">Mark unsafe areas and receive immediate alerts when any member enters them.</span></div>
      <div class="feature-item"><span class="feature-name">Home Detection</span><span class="feature-desc">WiFi SSID-based home arrival detection for seamless zone transitions.</span></div>
      <div class="feature-item"><span class="feature-name">School / Work Zones</span><span class="feature-desc">Specialized notifications for routine locations like school and office.</span></div>
    </div>
  </div>

  <div class="feature-section">
    <h3>📱 Communication &amp; Device Health</h3>
    <div class="feature-grid">
      <div class="feature-item"><span class="feature-name">Push Notifications</span><span class="feature-desc">Firebase Cloud Messaging delivers instant alerts bypassing Do Not Disturb.</span></div>
      <div class="feature-item"><span class="feature-name">Emergency Call</span><span class="feature-desc">One-tap calling to the primary emergency contact from any screen.</span></div>
      <div class="feature-item"><span class="feature-name">Morse Vibration</span><span class="feature-desc">SOS pattern vibration (··· — ···) for silent incoming alert acknowledgment.</span></div>
      <div class="feature-item"><span class="feature-name">Battery Alerts</span><span class="feature-desc">Notifies family when any member's battery drops to a critical level.</span></div>
      <div class="feature-item"><span class="feature-name">Signal Loss Alerts</span><span class="feature-desc">Immediate warning pushed to family when a member's phone goes offline.</span></div>
      <div class="feature-item"><span class="feature-name">Night Mode</span><span class="feature-desc">Enhanced sensitivity thresholds automatically applied during late-night hours.</span></div>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ ARCHITECTURE ══ -->
  <h2><span class="icon">🏗️</span> Architecture</h2>
  <p>Aegis follows <strong>Clean Architecture</strong> with strict layer separation. Each layer communicates only with the one below it — UI never touches Firebase directly.</p>

  <!-- Architecture Layer Diagram (inline SVG) -->
  <div class="diagram-card">
    <div class="diagram-title">// System Architecture — Layer Overview</div>
    <svg width="100%" viewBox="0 0 880 380" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <marker id="arr" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(255,255,255,0.25)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="arr-g" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(74,222,128,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
      </defs>

      <!-- UI Layer -->
      <rect x="20" y="20" width="840" height="72" rx="10" fill="rgba(167,139,250,0.08)" stroke="rgba(167,139,250,0.35)" stroke-width="1"/>
      <text x="44" y="48" class="flow-text" font-family="Syne, sans-serif" font-weight="700" font-size="13" fill="#a78bfa">UI Layer</text>
      <text x="44" y="65" class="flow-sub">Jetpack Compose</text>
      <!-- Pills -->
      <rect x="180" y="32" width="140" height="28" rx="14" fill="rgba(167,139,250,0.1)" stroke="rgba(167,139,250,0.25)" stroke-width="0.8"/>
      <text x="250" y="51" class="flow-text" font-size="11" text-anchor="middle" fill="#c4b5fd">Compose Screens</text>
      <rect x="336" y="32" width="168" height="28" rx="14" fill="rgba(167,139,250,0.1)" stroke="rgba(167,139,250,0.25)" stroke-width="0.8"/>
      <text x="420" y="51" class="flow-text" font-size="11" text-anchor="middle" fill="#c4b5fd">ViewModels · StateFlow</text>
      <rect x="520" y="32" width="100" height="28" rx="14" fill="rgba(167,139,250,0.1)" stroke="rgba(167,139,250,0.25)" stroke-width="0.8"/>
      <text x="570" y="51" class="flow-text" font-size="11" text-anchor="middle" fill="#c4b5fd">Navigation</text>

      <!-- Arrow -->
      <line x1="440" y1="92" x2="440" y2="116" stroke="rgba(255,255,255,0.2)" stroke-width="1.2" marker-end="url(#arr)"/>

      <!-- Domain Layer -->
      <rect x="20" y="118" width="840" height="72" rx="10" fill="rgba(34,211,238,0.06)" stroke="rgba(34,211,238,0.3)" stroke-width="1"/>
      <text x="44" y="146" class="flow-text" font-family="Syne, sans-serif" font-weight="700" font-size="13" fill="#22d3ee">Domain Layer</text>
      <text x="44" y="163" class="flow-sub">Business Logic</text>
      <rect x="180" y="130" width="120" height="28" rx="14" fill="rgba(34,211,238,0.08)" stroke="rgba(34,211,238,0.2)" stroke-width="0.8"/>
      <text x="240" y="149" class="flow-text" font-size="11" text-anchor="middle" fill="#67e8f9">Repositories</text>
      <rect x="316" y="130" width="148" height="28" rx="14" fill="rgba(34,211,238,0.08)" stroke="rgba(34,211,238,0.2)" stroke-width="0.8"/>
      <text x="390" y="149" class="flow-text" font-size="11" text-anchor="middle" fill="#67e8f9">Data Models</text>
      <rect x="480" y="130" width="140" height="28" rx="14" fill="rgba(34,211,238,0.08)" stroke="rgba(34,211,238,0.2)" stroke-width="0.8"/>
      <text x="550" y="149" class="flow-text" font-size="11" text-anchor="middle" fill="#67e8f9">Use Cases</text>

      <!-- Arrow -->
      <line x1="440" y1="190" x2="440" y2="214" stroke="rgba(255,255,255,0.2)" stroke-width="1.2" marker-end="url(#arr)"/>

      <!-- Data Layer -->
      <rect x="20" y="216" width="840" height="72" rx="10" fill="rgba(251,146,60,0.06)" stroke="rgba(251,146,60,0.28)" stroke-width="1"/>
      <text x="44" y="244" class="flow-text" font-family="Syne, sans-serif" font-weight="700" font-size="13" fill="#fb923c">Data Layer</text>
      <text x="44" y="261" class="flow-sub">Remote &amp; Local</text>
      <rect x="180" y="228" width="260" height="28" rx="14" fill="rgba(251,146,60,0.08)" stroke="rgba(251,146,60,0.2)" stroke-width="0.8"/>
      <text x="310" y="247" class="flow-text" font-size="11" text-anchor="middle" fill="#fdba74">Firebase · Auth / DB / Firestore / FCM</text>
      <rect x="456" y="228" width="180" height="28" rx="14" fill="rgba(251,146,60,0.08)" stroke="rgba(251,146,60,0.2)" stroke-width="0.8"/>
      <text x="546" y="247" class="flow-text" font-size="11" text-anchor="middle" fill="#fdba74">Room DB · DataStore</text>

      <!-- Arrow -->
      <line x1="440" y1="288" x2="440" y2="312" stroke="rgba(255,255,255,0.2)" stroke-width="1.2" marker-end="url(#arr)"/>

      <!-- Platform Layer -->
      <rect x="20" y="314" width="840" height="52" rx="10" fill="rgba(244,114,182,0.06)" stroke="rgba(244,114,182,0.28)" stroke-width="1"/>
      <text x="44" y="336" class="flow-text" font-family="Syne, sans-serif" font-weight="700" font-size="13" fill="#f472b6">Platform Layer</text>
      <rect x="230" y="324" width="172" height="28" rx="14" fill="rgba(244,114,182,0.08)" stroke="rgba(244,114,182,0.2)" stroke-width="0.8"/>
      <text x="316" y="343" class="flow-text" font-size="11" text-anchor="middle" fill="#f9a8d4">Foreground Services</text>
      <rect x="418" y="324" width="120" height="28" rx="14" fill="rgba(244,114,182,0.08)" stroke="rgba(244,114,182,0.2)" stroke-width="0.8"/>
      <text x="478" y="343" class="flow-text" font-size="11" text-anchor="middle" fill="#f9a8d4">Receivers</text>
      <rect x="554" y="324" width="104" height="28" rx="14" fill="rgba(244,114,182,0.08)" stroke="rgba(244,114,182,0.2)" stroke-width="0.8"/>
      <text x="606" y="343" class="flow-text" font-size="11" text-anchor="middle" fill="#f9a8d4">System APIs</text>
    </svg>
  </div>

  <!-- SOS Flow Diagram -->
  <h3>🚨 SOS Emergency Flow</h3>
  <div class="diagram-card">
    <div class="diagram-title">// SOS trigger → countdown → alert → delivery</div>
    <svg width="100%" viewBox="0 0 880 480" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <marker id="fa" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(255,255,255,0.22)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="fa-g" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(74,222,128,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="fa-r" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(248,113,113,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="fa-b" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(34,211,238,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="fa-y" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(251,146,60,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
      </defs>

      <!-- User triggers -->
      <rect x="340" y="10" width="200" height="36" rx="8" fill="rgba(248,113,113,0.12)" stroke="rgba(248,113,113,0.4)" stroke-width="1"/>
      <text x="440" y="33" font-family="DM Sans,sans-serif" font-size="12" fill="#f87171" text-anchor="middle">User triggers SOS</text>

      <!-- Trigger arrows down to 5 nodes -->
      <line x1="390" y1="46" x2="120" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="410" y1="46" x2="250" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="440" y1="46" x2="440" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="470" y1="46" x2="630" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="490" y1="46" x2="790" y2="100" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>

      <!-- 5 trigger nodes -->
      <rect x="40"  y="100" width="160" height="44" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="120" y="118" font-family="DM Mono,monospace" font-size="10" fill="#67e8f9" text-anchor="middle">Manual</text>
      <text x="120" y="132" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">SOS Button tap</text>

      <rect x="174" y="100" width="152" height="44" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="250" y="118" font-family="DM Mono,monospace" font-size="10" fill="#67e8f9" text-anchor="middle">Auto Crash</text>
      <text x="250" y="132" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">Accel &gt; 3.5G</text>

      <rect x="360" y="100" width="160" height="44" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="440" y="118" font-family="DM Mono,monospace" font-size="10" fill="#67e8f9" text-anchor="middle">Shake</text>
      <text x="440" y="132" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">5× in 2 seconds</text>

      <rect x="550" y="100" width="160" height="44" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="630" y="118" font-family="DM Mono,monospace" font-size="10" fill="#67e8f9" text-anchor="middle">Volume Button</text>
      <text x="630" y="132" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">3× press</text>

      <rect x="718" y="100" width="144" height="44" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="790" y="118" font-family="DM Mono,monospace" font-size="10" fill="#67e8f9" text-anchor="middle">Power Button</text>
      <text x="790" y="132" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">5× rapid press</text>

      <!-- All converge to Countdown -->
      <line x1="120" y1="144" x2="380" y2="198" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="250" y1="144" x2="400" y2="198" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="440" y1="144" x2="440" y2="198" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="630" y1="144" x2="480" y2="198" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="790" y1="144" x2="500" y2="198" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#fa)"/>

      <!-- Countdown -->
      <rect x="330" y="200" width="220" height="40" rx="8" fill="rgba(251,146,60,0.1)" stroke="rgba(251,146,60,0.4)" stroke-width="1"/>
      <text x="440" y="225" font-family="DM Sans,sans-serif" font-size="12" fill="#fb923c" text-anchor="middle">30-second countdown</text>

      <!-- Cancelled / Not Cancelled branches -->
      <line x1="330" y1="220" x2="160" y2="268" stroke="rgba(248,113,113,0.5)" stroke-width="1" marker-end="url(#fa-r)"/>
      <text x="215" y="255" font-family="DM Mono,monospace" font-size="9" fill="#f87171">cancelled</text>
      <line x1="550" y1="220" x2="700" y2="268" stroke="rgba(74,222,128,0.5)" stroke-width="1" marker-end="url(#fa-g)"/>
      <text x="650" y="255" font-family="DM Mono,monospace" font-size="9" fill="#4ade80">not cancelled</text>

      <!-- Cancelled box -->
      <rect x="60" y="270" width="180" height="36" rx="8" fill="rgba(248,113,113,0.08)" stroke="rgba(248,113,113,0.3)" stroke-width="0.8"/>
      <text x="150" y="293" font-family="DM Sans,sans-serif" font-size="11" fill="#f87171" text-anchor="middle">Alert cancelled ✓</text>

      <!-- Send SOS -->
      <rect x="620" y="270" width="200" height="36" rx="8" fill="rgba(74,222,128,0.1)" stroke="rgba(74,222,128,0.4)" stroke-width="1"/>
      <text x="720" y="293" font-family="DM Sans,sans-serif" font-size="12" fill="#4ade80" text-anchor="middle">Send SOS Alert</text>

      <!-- Firebase reachable -->
      <line x1="720" y1="306" x2="560" y2="356" stroke="rgba(34,211,238,0.5)" stroke-width="1" marker-end="url(#fa-b)"/>
      <text x="605" y="335" font-family="DM Mono,monospace" font-size="9" fill="#67e8f9">reachable</text>
      <line x1="720" y1="306" x2="840" y2="356" stroke="rgba(251,146,60,0.5)" stroke-width="1" marker-end="url(#fa-y)"/>
      <text x="796" y="335" font-family="DM Mono,monospace" font-size="9" fill="#fb923c">offline</text>

      <!-- Firebase & SMS boxes -->
      <rect x="440" y="358" width="200" height="36" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.3)" stroke-width="0.8"/>
      <text x="540" y="381" font-family="DM Sans,sans-serif" font-size="11" fill="#67e8f9" text-anchor="middle">Push to Firebase RTDB</text>

      <rect x="748" y="358" width="120" height="36" rx="8" fill="rgba(251,146,60,0.07)" stroke="rgba(251,146,60,0.3)" stroke-width="0.8"/>
      <text x="808" y="381" font-family="DM Sans,sans-serif" font-size="11" fill="#fb923c" text-anchor="middle">SMS Fallback</text>

      <!-- Both → FCM -->
      <line x1="540" y1="394" x2="540" y2="430" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>
      <line x1="808" y1="394" x2="600" y2="430" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#fa)"/>

      <rect x="390" y="432" width="300" height="36" rx="8" fill="rgba(167,139,250,0.08)" stroke="rgba(167,139,250,0.35)" stroke-width="1"/>
      <text x="540" y="455" font-family="DM Sans,sans-serif" font-size="12" fill="#a78bfa" text-anchor="middle">FCM → contacts receive alert + location</text>
    </svg>
  </div>

  <!-- Location Flow -->
  <h3>📍 Location Tracking Flow</h3>
  <div class="diagram-card">
    <div class="diagram-title">// App launch → permissions → service → sync → geofence check</div>
    <svg width="100%" viewBox="0 0 880 400" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <marker id="la" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(255,255,255,0.22)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="la-g" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(74,222,128,0.7)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="la-r" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(248,113,113,0.6)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
        <marker id="la-b" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
          <path d="M2 1L8 5L2 9" fill="none" stroke="rgba(34,211,238,0.7)" stroke-width="1.5" stroke-linecap="round"/>
        </marker>
      </defs>

      <!-- Row 1 -->
      <rect x="20" y="14" width="168" height="40" rx="8" fill="rgba(74,222,128,0.08)" stroke="rgba(74,222,128,0.3)" stroke-width="1"/>
      <text x="104" y="39" font-family="DM Sans,sans-serif" font-size="12" fill="#4ade80" text-anchor="middle">App Launched</text>

      <line x1="188" y1="34" x2="228" y2="34" stroke="rgba(255,255,255,0.2)" stroke-width="1" marker-end="url(#la)"/>

      <rect x="230" y="14" width="192" height="40" rx="8" fill="rgba(255,255,255,0.04)" stroke="rgba(255,255,255,0.12)" stroke-width="0.8"/>
      <text x="326" y="39" font-family="DM Sans,sans-serif" font-size="11" fill="#e8eaf0" text-anchor="middle">Request Location Permissions</text>

      <line x1="422" y1="34" x2="462" y2="34" stroke="rgba(255,255,255,0.2)" stroke-width="1" marker-end="url(#la)"/>

      <!-- Permissions granted? diamond -->
      <polygon points="540,14 620,34 540,54 460,34" fill="rgba(251,146,60,0.08)" stroke="rgba(251,146,60,0.35)" stroke-width="1"/>
      <text x="540" y="39" font-family="DM Mono,monospace" font-size="10" fill="#fb923c" text-anchor="middle">Granted?</text>

      <!-- No branch -->
      <line x1="540" y1="54" x2="540" y2="90" stroke="rgba(248,113,113,0.5)" stroke-width="1" marker-end="url(#la-r)"/>
      <text x="548" y="76" font-family="DM Mono,monospace" font-size="9" fill="#f87171">no</text>
      <rect x="460" y="92" width="160" height="36" rx="8" fill="rgba(248,113,113,0.07)" stroke="rgba(248,113,113,0.3)" stroke-width="0.8"/>
      <text x="540" y="115" font-family="DM Sans,sans-serif" font-size="11" fill="#f87171" text-anchor="middle">Show rationale</text>

      <!-- Yes branch -->
      <line x1="620" y1="34" x2="720" y2="34" stroke="rgba(74,222,128,0.5)" stroke-width="1" marker-end="url(#la-g)"/>
      <text x="670" y="28" font-family="DM Mono,monospace" font-size="9" fill="#4ade80">yes</text>
      <rect x="722" y="14" width="140" height="40" rx="8" fill="rgba(74,222,128,0.08)" stroke="rgba(74,222,128,0.3)" stroke-width="1"/>
      <text x="792" y="30" font-family="DM Sans,sans-serif" font-size="11" fill="#4ade80" text-anchor="middle">Start Foreground</text>
      <text x="792" y="46" font-family="DM Sans,sans-serif" font-size="11" fill="#4ade80" text-anchor="middle">Service</text>

      <!-- Row 2 -->
      <line x1="792" y1="54" x2="792" y2="104" stroke="rgba(34,211,238,0.4)" stroke-width="1" marker-end="url(#la-b)"/>
      <rect x="692" y="106" width="200" height="40" rx="8" fill="rgba(34,211,238,0.07)" stroke="rgba(34,211,238,0.28)" stroke-width="0.8"/>
      <text x="792" y="131" font-family="DM Sans,sans-serif" font-size="12" fill="#67e8f9" text-anchor="middle">FusedLocationProvider</text>

      <!-- Active / Passive -->
      <line x1="692" y1="126" x2="580" y2="160" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#la)"/>
      <text x="605" y="148" font-family="DM Mono,monospace" font-size="9" fill="#7a7f8e">active</text>

      <rect x="440" y="162" width="160" height="36" rx="8" fill="rgba(255,255,255,0.04)" stroke="rgba(255,255,255,0.1)" stroke-width="0.8"/>
      <text x="520" y="180" font-family="DM Mono,monospace" font-size="10" fill="#e8eaf0" text-anchor="middle">Every 5s / 3m delta</text>

      <line x1="692" y1="126" x2="760" y2="160" stroke="rgba(255,255,255,0.15)" stroke-width="1" marker-end="url(#la)"/>
      <text x="736" y="148" font-family="DM Mono,monospace" font-size="9" fill="#7a7f8e">passive</text>
      <rect x="660" y="162" width="172" height="36" rx="8" fill="rgba(255,255,255,0.04)" stroke="rgba(255,255,255,0.1)" stroke-width="0.8"/>
      <text x="746" y="180" font-family="DM Mono,monospace" font-size="10" fill="#e8eaf0" text-anchor="middle">Every 30s / 20m delta</text>

      <!-- Converge -->
      <line x1="520" y1="198" x2="520" y2="240" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#la)"/>
      <line x1="746" y1="198" x2="600" y2="240" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#la)"/>

      <!-- Network? -->
      <polygon points="560,240 640,260 560,280 480,260" fill="rgba(251,146,60,0.08)" stroke="rgba(251,146,60,0.35)" stroke-width="1"/>
      <text x="560" y="265" font-family="DM Mono,monospace" font-size="10" fill="#fb923c" text-anchor="middle">Network?</text>

      <line x1="480" y1="260" x2="360" y2="300" stroke="rgba(248,113,113,0.5)" stroke-width="1" marker-end="url(#la-r)"/>
      <text x="400" y="285" font-family="DM Mono,monospace" font-size="9" fill="#f87171">offline</text>
      <rect x="224" y="302" width="172" height="36" rx="8" fill="rgba(248,113,113,0.07)" stroke="rgba(248,113,113,0.28)" stroke-width="0.8"/>
      <text x="310" y="325" font-family="DM Sans,sans-serif" font-size="11" fill="#f87171" text-anchor="middle">Queue in Room DB</text>

      <line x1="640" y1="260" x2="750" y2="300" stroke="rgba(74,222,128,0.5)" stroke-width="1" marker-end="url(#la-g)"/>
      <text x="710" y="285" font-family="DM Mono,monospace" font-size="9" fill="#4ade80">online</text>
      <rect x="670" y="302" width="180" height="36" rx="8" fill="rgba(74,222,128,0.08)" stroke="rgba(74,222,128,0.3)" stroke-width="1"/>
      <text x="760" y="320" font-family="DM Sans,sans-serif" font-size="11" fill="#4ade80" text-anchor="middle">Sync to Firebase RTDB</text>
      <text x="760" y="334" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">+ check geofences</text>

      <!-- Flush -->
      <line x1="310" y1="338" x2="310" y2="370" stroke="rgba(255,255,255,0.12)" stroke-width="1" marker-end="url(#la)"/>
      <rect x="204" y="372" width="212" height="24" rx="6" fill="rgba(255,255,255,0.03)" stroke="rgba(255,255,255,0.08)" stroke-width="0.6"/>
      <text x="310" y="388" font-family="DM Sans,sans-serif" font-size="10" fill="#7a7f8e" text-anchor="middle">Flush queue when back online</text>
    </svg>
  </div>

  <div class="divider"></div>

  <!-- ══ TECH STACK ══ -->
  <h2><span class="icon">🛠️</span> Technology Stack</h2>

  <div class="tech-grid">
    <div class="tech-item"><span class="tech-cat">Language</span><span class="tech-name">Kotlin 1.9.x</span><span class="tech-desc">Primary development language</span></div>
    <div class="tech-item"><span class="tech-cat">UI Framework</span><span class="tech-name">Jetpack Compose</span><span class="tech-desc">Modern declarative UI toolkit</span></div>
    <div class="tech-item"><span class="tech-cat">Design System</span><span class="tech-name">Material Design 3</span><span class="tech-desc">Consistent, accessible UI</span></div>
    <div class="tech-item"><span class="tech-cat">Architecture</span><span class="tech-name">MVVM + Clean Arch</span><span class="tech-desc">Strict separation of concerns</span></div>
    <div class="tech-item"><span class="tech-cat">DI</span><span class="tech-name">Hilt</span><span class="tech-desc">Compile-time injection</span></div>
    <div class="tech-item"><span class="tech-cat">Auth</span><span class="tech-name">Firebase Auth</span><span class="tech-desc">Phone &amp; Email authentication</span></div>
    <div class="tech-item"><span class="tech-cat">Realtime DB</span><span class="tech-name">Firebase RTDB</span><span class="tech-desc">Live location &amp; alert sync</span></div>
    <div class="tech-item"><span class="tech-cat">Document DB</span><span class="tech-name">Cloud Firestore</span><span class="tech-desc">User data &amp; FCM queue</span></div>
    <div class="tech-item"><span class="tech-cat">Notifications</span><span class="tech-name">Firebase FCM</span><span class="tech-desc">Instant push delivery</span></div>
    <div class="tech-item"><span class="tech-cat">Maps</span><span class="tech-name">Google Maps SDK</span><span class="tech-desc">Location visualization</span></div>
    <div class="tech-item"><span class="tech-cat">Location</span><span class="tech-name">FusedLocationProvider</span><span class="tech-desc">Battery-efficient GPS</span></div>
    <div class="tech-item"><span class="tech-cat">Sensors</span><span class="tech-name">SensorManager</span><span class="tech-desc">Accelerometer + Gyroscope</span></div>
    <div class="tech-item"><span class="tech-cat">ML</span><span class="tech-name">ML Kit Face Detection</span><span class="tech-desc">Driver fatigue monitoring</span></div>
    <div class="tech-item"><span class="tech-cat">Camera</span><span class="tech-name">CameraX</span><span class="tech-desc">Camera integration</span></div>
    <div class="tech-item"><span class="tech-cat">Background</span><span class="tech-name">ForegroundService</span><span class="tech-desc">Persistent tracking</span></div>
    <div class="tech-item"><span class="tech-cat">Local DB</span><span class="tech-name">Room Database</span><span class="tech-desc">Offline location queue</span></div>
    <div class="tech-item"><span class="tech-cat">Preferences</span><span class="tech-name">DataStore</span><span class="tech-desc">App settings storage</span></div>
  </div>

  <div class="divider"></div>

  <!-- ══ PROJECT STRUCTURE ══ -->
  <h2><span class="icon">📂</span> Project Structure</h2>

  <pre><span class="code-path">Aegis/</span>
├── <span class="code-path">app/src/main/</span>
│   ├── <span class="code-path">java/com/karthik/aegis/</span>
│   │   ├── <span class="code-file">AegisApplication.kt</span>         <span class="code-comment"># Hilt Application entry point</span>
│   │   ├── <span class="code-path">di/</span><span class="code-file">AppModule.kt</span>             <span class="code-comment"># Dependency Injection (Hilt + Room)</span>
│   │   ├── <span class="code-path">model/</span><span class="code-file">Models.kt</span>              <span class="code-comment"># Data models with Room entities</span>
│   │   ├── <span class="code-path">data/local/</span>
│   │   │   ├── <span class="code-file">AppDatabase.kt</span>           <span class="code-comment"># Room database definition</span>
│   │   │   └── <span class="code-path">dao/</span>
│   │   │       ├── <span class="code-file">OfflineLocationDao.kt</span>
│   │   │       ├── <span class="code-file">AlertHistoryDao.kt</span>
│   │   │       └── <span class="code-file">SafetyScoreDao.kt</span>
│   │   ├── <span class="code-path">repository/</span>
│   │   │   ├── <span class="code-file">SOSRepository.kt</span>
│   │   │   ├── <span class="code-file">ContactsRepository.kt</span>
│   │   │   ├── <span class="code-file">FamilyRepository.kt</span>
│   │   │   ├── <span class="code-file">LocationRepository.kt</span>
│   │   │   └── <span class="code-file">ZoneRepository.kt</span>
│   │   ├── <span class="code-path">service/</span>
│   │   │   ├── <span class="code-file">LocationTrackingService.kt</span>
│   │   │   ├── <span class="code-file">AccidentDetectorService.kt</span>
│   │   │   ├── <span class="code-file">SOSBroadcastReceiver.kt</span>
│   │   │   ├── <span class="code-file">BootReceiver.kt</span>
│   │   │   └── <span class="code-file">AegisFirebaseMessagingService.kt</span>
│   │   ├── <span class="code-path">ui/</span>
│   │   │   ├── <span class="code-file">MainActivity.kt</span>
│   │   │   ├── <span class="code-path">navigation/</span><span class="code-file">NavHost.kt</span>
│   │   │   ├── <span class="code-path">splash/</span><span class="code-file">SplashScreen.kt</span>
│   │   │   ├── <span class="code-path">auth/</span><span class="code-file">AuthScreen.kt</span>
│   │   │   ├── <span class="code-path">home/</span><span class="code-file">HomeScreen.kt</span>
│   │   │   ├── <span class="code-path">contacts/</span>
│   │   │   │   ├── <span class="code-file">ContactsScreen.kt</span>
│   │   │   │   └── <span class="code-file">ContactsViewModel.kt</span>
│   │   │   └── <span class="code-path">theme/</span><span class="code-file">Theme.kt</span>
│   │   └── <span class="code-path">utils/</span>
│   │       ├── <span class="code-file">AegisPrefs.kt</span>
│   │       ├── <span class="code-file">DistanceUtils.kt</span>
│   │       └── <span class="code-file">NotificationUtils.kt</span>
│   ├── <span class="code-path">res/</span>                             <span class="code-comment"># Android resources</span>
│   └── <span class="code-file">AndroidManifest.xml</span>
├── <span class="code-file">build.gradle.kts</span>                   <span class="code-comment"># Root build config</span>
├── <span class="code-file">settings.gradle.kts</span>               <span class="code-comment"># Project settings</span>
└── <span class="code-file">README.md</span></pre>

  <div class="divider"></div>

  <!-- ══ GETTING STARTED ══ -->
  <h2><span class="icon">🚀</span> Getting Started</h2>

  <h3>Prerequisites</h3>
  <table class="req-table">
    <thead><tr><th>Requirement</th><th>Version</th></tr></thead>
    <tbody>
      <tr><td>Android Studio</td><td>Hedgehog (2023.1.1) or later</td></tr>
      <tr><td>Java Development Kit</td><td><code>JDK 17</code></td></tr>
      <tr><td>Android SDK</td><td><code>API 26+</code> (minSdk)</td></tr>
      <tr><td>Target SDK</td><td><code>API 34</code></td></tr>
    </tbody>
  </table>

  <h3>Installation</h3>
  <div class="steps">
    <div class="step">
      <div class="step-num">1</div>
      <div class="step-body">
        <div class="step-title">Clone the Repository</div>
        <pre><span class="code-keyword">git</span> clone https://github.com/MKarthik730/aegis.git
<span class="code-keyword">cd</span> aegis</pre>
      </div>
    </div>
    <div class="step">
      <div class="step-num">2</div>
      <div class="step-body">
        <div class="step-title">Create Firebase Project</div>
        <p style="margin-bottom:8px">Go to <a href="https://console.firebase.google.com">Firebase Console</a> and create a project named <strong>Aegis</strong>. Enable: Authentication (Phone + Email), Realtime Database, Cloud Firestore, and Cloud Messaging. Add an Android app with package <code>com.karthik.aegis</code> and download <code>google-services.json</code> into <code>/app/</code>.</p>
      </div>
    </div>
    <div class="step">
      <div class="step-num">3</div>
      <div class="step-body">
        <div class="step-title">Configure Google Maps API</div>
        <pre><span class="code-comment"># gradle.properties</span>
MAPS_API_KEY=<span class="code-string">your_actual_api_key_here</span></pre>
        <p>Enable Maps SDK for Android in <a href="https://console.cloud.google.com">Google Cloud Console</a> and create an API key.</p>
      </div>
    </div>
    <div class="step">
      <div class="step-num">4</div>
      <div class="step-body">
        <div class="step-title">Build the Project</div>
        <pre>./gradlew assembleDebug</pre>
      </div>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ PERMISSIONS ══ -->
  <h2><span class="icon">⚙️</span> Permissions</h2>
  <div class="perms-grid">
    <div class="perm-item"><span class="perm-name">ACCESS_FINE_LOCATION</span><span class="perm-desc">GPS location tracking</span></div>
    <div class="perm-item"><span class="perm-name">FOREGROUND_SERVICE</span><span class="perm-desc">Persistent background tracking</span></div>
    <div class="perm-item"><span class="perm-name">CAMERA</span><span class="perm-desc">ML Kit face detection for fatigue</span></div>
    <div class="perm-item"><span class="perm-name">POST_NOTIFICATIONS</span><span class="perm-desc">Push notification display</span></div>
    <div class="perm-item"><span class="perm-name">SEND_SMS</span><span class="perm-desc">Offline SOS via SMS fallback</span></div>
    <div class="perm-item"><span class="perm-name">RECEIVE_BOOT_COMPLETED</span><span class="perm-desc">Auto-start services on device boot</span></div>
  </div>

  <div class="divider"></div>

  <!-- ══ ROADMAP ══ -->
  <h2><span class="icon">🗺️</span> Roadmap</h2>
  <div class="roadmap">
    <div class="roadmap-item">
      <div class="roadmap-dot dot-done">✓</div>
      <div class="roadmap-content">
        <div class="roadmap-phase">Phase 1 — Core</div>
        <div class="roadmap-features">SOS system · Location tracking · Crash detection · Geofencing</div>
      </div>
      <span class="badge green" style="margin-left:auto;align-self:center">Complete</span>
    </div>
    <div class="roadmap-item">
      <div class="roadmap-dot dot-active">◎</div>
      <div class="roadmap-content">
        <div class="roadmap-phase">Phase 2 — Intelligence</div>
        <div class="roadmap-features">AI threat detection · Safety scoring engine</div>
      </div>
      <span class="badge orange" style="margin-left:auto;align-self:center">In Progress</span>
    </div>
    <div class="roadmap-item">
      <div class="roadmap-dot dot-plan">○</div>
      <div class="roadmap-content">
        <div class="roadmap-phase">Phase 3 — Connectivity</div>
        <div class="roadmap-features">Bluetooth mesh networking · Wi-Fi Direct communication</div>
      </div>
      <span class="badge gray" style="margin-left:auto;align-self:center">Planned</span>
    </div>
    <div class="roadmap-item">
      <div class="roadmap-dot dot-plan">○</div>
      <div class="roadmap-content">
        <div class="roadmap-phase">Phase 4 — UX</div>
        <div class="roadmap-features">Route playback · Voice SOS · Home screen widgets</div>
      </div>
      <span class="badge gray" style="margin-left:auto;align-self:center">Planned</span>
    </div>
    <div class="roadmap-item">
      <div class="roadmap-dot dot-plan">○</div>
      <div class="roadmap-content">
        <div class="roadmap-phase">Phase 5 — Release</div>
        <div class="roadmap-features">Beta testing program · Google Play Store submission</div>
      </div>
      <span class="badge gray" style="margin-left:auto;align-self:center">Planned</span>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ CONTRIBUTING ══ -->
  <h2><span class="icon">🤝</span> Contributing</h2>
  <div class="steps">
    <div class="step">
      <div class="step-num">1</div>
      <div class="step-body"><div class="step-title">Fork the repository</div><pre><span class="code-keyword">git</span> fork https://github.com/MKarthik730/aegis.git</pre></div>
    </div>
    <div class="step">
      <div class="step-num">2</div>
      <div class="step-body"><div class="step-title">Create a feature branch</div><pre><span class="code-keyword">git</span> checkout -b feature/your-feature</pre></div>
    </div>
    <div class="step">
      <div class="step-num">3</div>
      <div class="step-body"><div class="step-title">Commit and push</div><pre><span class="code-keyword">git</span> push origin feature/your-feature</pre></div>
    </div>
    <div class="step">
      <div class="step-num">4</div>
      <div class="step-body"><div class="step-title">Open a Pull Request</div><p>Describe your changes and link any relevant issues.</p></div>
    </div>
  </div>

  <div class="divider"></div>

  <!-- ══ LICENSE ══ -->
  <h2><span class="icon">📄</span> License</h2>
  <div class="card" style="max-width:480px">
    <div class="card-title">MIT License</div>
    <div class="card-desc" style="margin-top:6px">Permission is hereby granted, free of charge, to any person obtaining a copy of this software, to deal in the Software without restriction. See the LICENSE file for the full text.</div>
  </div>

  <!-- ══ FOOTER ══ -->
  <div class="footer">
    <svg width="40" height="40" viewBox="0 0 72 72" fill="none" style="opacity:0.5">
      <path d="M36 6L10 18V38C10 52.4 21.6 65.8 36 69C50.4 65.8 62 52.4 62 38V18L36 6Z" stroke="#4ade80" stroke-width="1.5" fill="rgba(74,222,128,0.06)"/>
      <path d="M26 36L32 42L46 28" stroke="#4ade80" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
    <div class="footer-logo">Aegis</div>
    <div class="footer-text">
      Built by <a href="https://github.com/MKarthik730">Karthik Motupalli</a> · B.Tech CSE, ANITS Visakhapatnam
    </div>
    <div class="footer-text">Built with Kotlin, Jetpack Compose, and Firebase · Your family. Protected. Always.</div>
  </div>

</div>
</body>
</html>