# Java Swing UI & Multi-Role Demo Guidelines

## 1. JScrollPane Viewport & Empty State Handling
When replacing a `JTable` inside a `JScrollPane` with an empty state panel:
- **Implement `Scrollable`**: Have the empty state panel implement `javax.swing.Scrollable`, returning `true` for both `getScrollableTracksViewportWidth()` and `getScrollableTracksViewportHeight()`.
- **Center Alignment with GridBagLayout**: Use `GridBagLayout` with `GridBagConstraints.CENTER` on the outer panel and place content inside an inner bordered card to guarantee horizontal and vertical symmetry regardless of viewport resizing.
- **Toggle Column Headers**: Always call `scrollPane.setColumnHeaderView(null)` when showing an empty state, and restore `scrollPane.setColumnHeaderView(table.getTableHeader())` when displaying table records, followed by `revalidate()` and `repaint()`.

## 2. Multi-Role Demo Resilience
When designing UI panels and data queries for evaluation/demo modes:
- **Role Fallback**: If an active session is in a different role (e.g., student viewing owner portal tabs), resolve owner queries to the demo owner portfolio (e.g., `"owner"`) rather than failing or returning 0 records.
- **Tab Auto-Refresh**: Add `ChangeListener` to `JTabbedPane` so that switching tabs automatically refreshes underlying tables and badges without requiring a manual refresh click.
- **Role-Based Portal Access (Strict RBAC)**: Students must never be provided an option to switch to the Owner portal. The Owner portal is strictly reserved for Owner accounts to view and manage their listings and review student enquiries. Portal mode switching controls are only available to Owners (to preview the public Student View) and Admins.

## 3. Map & External Binary Caches
- Any tile caches or network caches (such as JXMapViewer2 OSM tiles in `data/cache/`) must be explicitly ignored in `.gitignore` prior to staging git commits to prevent binary repository bloat.
