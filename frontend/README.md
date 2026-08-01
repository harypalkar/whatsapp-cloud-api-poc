# Altitude Labs Angular 20 Dashboard

Scaffold for:

- Live Chat
- Customer List
- Campaign Builder
- Conversation Timeline
- Delivery / Read / Failed reports

## Bootstrap

```bash
npx -y @angular/cli@20 new altitude-dashboard --directory . --routing --style=scss --ssr=false
npm install @angular/material chart.js bootstrap
ng serve
```

Point the Angular `environment.apiBase` to `http://localhost:8080`.

Until the full Angular app is generated, use [`../dashboard/index.html`](../dashboard/index.html) for live E2E testing.
