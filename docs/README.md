# Evidence checklist

This folder holds the screenshots the main [README](../README.md#11-evidence-and-results)
embeds in Section 11. Drop each file in here with the exact name below and the corresponding
image in the main README will render automatically on GitHub — nothing else needs to change.

Until a file exists, GitHub shows a broken-image icon where it belongs; that is expected while
this checklist is still in progress, but every row should be checked off before the final
submission.

| # | Filename | What it should show | Lab section |
|---|---|---|---|
| 1 | `local-baseline.png` | Server console printing the request line, next to the browser showing the baseline HTML response | 2.1 |
| 2 | `local-sequential.png` | Server log with several consecutive requests answered by the same running process (no restart) | 2.2 |
| 3 | `network-static.png` | Browser DevTools Network tab: separate `200` requests for the HTML, CSS, JS, and both images, each with the correct `Content-Type` | 3 |
| 4 | `response-errors.png` | A `404` for a missing file, a `405` for a non-GET method, and a rejected path-traversal attempt | 3.1 |
| 5 | `services.png` | One valid and one invalid response for each of the four hardcoded services | 4 |
| 6 | `async-client.png` | A successful greeting/square/time request updating the result area with no page reload (URL bar unchanged) | 5 |
| 7 | `async-client-error.png` | An invalid input (e.g. a non-numeric square) rendered as a friendly message in the error area | 5 |
| 8 | `sequential-two-windows.png` | Two browser windows side by side: the second window's request sitting `(pending)` while `/api/slow` runs in the first | 6.2 |
| 9 | `security-group.png` | EC2 security group inbound rules: SSH restricted to one IP, the app port scoped as the instructor allowed | 7.2 |
| 10 | `ec2-health.png` | `curl http://localhost:<port>/api/health` run **from inside** the instance, before testing from a browser | 7.3 |
| 11 | `ec2-running.png` | The application loaded from the EC2 **public** address — page, script, images, and services all working | 7.3 |
| 12 | `ec2-systemd.png` | `systemctl status networking-lab` showing the service `active (running)` and `enabled` | 7.4 |
| 13 | `ec2-logout.png` | The public page still responding after the SSH/Session Manager session was closed | 7.4 |
| 14 | `ec2-terminated.png` | The EC2 console showing the instance state as `terminated` (Section 10 cleanup) | 10 |

Tips for capturing these cleanly:

- Crop out anything that identifies the instance (public IP/DNS, account ID, ARNs) unless the
  lab explicitly asks for it — the README already says no such detail should be published.
  A quick way to blur the address bar without hiding the useful part of a screenshot.
- For #8, DevTools' Network tab timestamps are more convincing than a stopwatch: keep the
  "Waterfall" column visible so the pending bar is clearly visible next to the slow request.
- Keep filenames exactly as listed (case-sensitive on GitHub's rendering, even though not on
  Windows) so the links in the main README don't break.
