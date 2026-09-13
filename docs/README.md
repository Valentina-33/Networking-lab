# Evidence checklist

This folder holds the screenshots that the main [README](../README.md#11-evidence-and-results)
embeds in Section 11. Drop each file in here with the exact name shown below, and the matching
image in the main README will render on its own on GitHub. Nothing else needs to change.

Until a file exists, GitHub shows a broken-image icon in its place. That is normal while this
checklist is still in progress, but every row should be checked off before the final submission.

| # | Filename | What it should show | Lab section |
|---|---|---|---|
| 1 | `local-baseline.png` | Server console printing the request line, next to the browser showing the baseline HTML response | 2.1 |
| 2 | `local-sequential.png` | Server log with several requests in a row, answered by the same running process (no restart) | 2.2 |
| 3 | `network-static.png` | Browser DevTools Network tab: separate `200` requests for the HTML, CSS, JS, and both images, each with the right `Content-Type` | 3 |
| 4 | `response-errors.png` | A `404` for a missing file, a `405` for a non-GET method, and a rejected path traversal attempt | 3.1 |
| 5 | `services.png` | One valid and one invalid response for each of the four hardcoded services | 4 |
| 6 | `async-client.png` | A successful greeting, square, or time request that updates the result area with no page reload (the URL bar stays the same) | 5 |
| 7 | `async-client-error.png` | An invalid input (for example a non-numeric square) shown as a friendly message in the error area | 5 |
| 8 | `sequential-two-windows.png` | Two browser windows side by side: the second window's request sitting `(pending)` while `/api/slow` runs in the first | 6.2 |
| 9 | `security-group.png` | EC2 security group inbound rules: SSH limited to one IP, the app port scoped the way the instructor allowed | 7.2 |
| 10 | `ec2-health.png` | `curl http://localhost:<port>/api/health` run **from inside** the instance, before testing it from a browser | 7.3 |
| 11 | `ec2-running.png` | The application loaded from the EC2 **public** address: page, script, images, and services all working | 7.3 |
| 12 | `ec2-systemd.png` | `systemctl status networking-lab` showing the service as `active (running)` and `enabled` | 7.4 |
| 13 | `ec2-logout.png` | The public page still responding after the SSH or Session Manager session was closed | 7.4 |
| 14 | `ec2-terminated.png` | The EC2 console showing the instance state as `terminated` (Section 10 cleanup) | 10 |

A few tips for taking these cleanly:

- Crop out anything that identifies the instance (public IP or DNS, account ID, ARNs) unless the
  lab specifically asks for it. The main README already says no such detail should be published.
- For #8, timestamps from DevTools' Network tab are more convincing than a stopwatch. Keep the
  "Waterfall" column visible so the pending bar next to the slow request is easy to see.
- Keep the filenames exactly as listed above. GitHub's rendering is case-sensitive even though
  Windows is not, so a mismatch would break the links in the main README.
