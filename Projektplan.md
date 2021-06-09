# Projektplan

| Datum | Arbeitspaket | Beschreibung |
| - | - | - |
| 19.05 | Projektantrag und Projektplan erstellen |  |
| 26.05 | <ul><li>Lokales Repository mit dem Remote Repository verbinden</li><li>Fragments</li><li>Layout</li><li>Preferences</li></ul> | <ul><li></li><li>NavigationDrawer zum Durchnavigieren zwischen den Fragments (Task planen, Automatische Antwort, Einstellungen). Außerdem eine **Master-Detail-Ansicht** je in den Fragments *Task planen* und *Automatische Antwort*. Masteransicht: Auflistung Tasks, Detailansicht: Details zum Task</li><li>Entsprechende `.xml` Layouts für die entsprechenden Activities und Fragments.</li><li>DarkTheme, Benachrichtigungen</li></ul> |
| 02.06 | <ul><li>Tabs</li><li>ListView (für die Tasks) und Adapter</li><li>Activity, die aufgerufen wird wenn der FloatingActionButton betätigt wird (neuer Task)</li></ul> | <ul><li>[Tabs](https://material.io/components/tabs) für die laufenden und abgeschlossenen Tasks</li></ul> |
| 09.06 | <ul><li>Dialog, der aufgerufen wird, wenn der FloatingActionButton betätigt wird (neuer Task)</li><li>Automatisch SMS versenden mit Location</li><li>Anbindung an Gmail-API</li></ul> | <ul><li>Dialog: Dabei soll u.a. auch auf die gespeicherten Kontakte zugegriffen werden</li><li>Implementieren der Features für die *Task planen* Funktion</li> |
| 16.06 | <ul><li>Alarm Manager</li><li>Automatisch Antworten für SMS</li><li>Notification Service</li></ul> | <ul><li>[AlarmManager](https://developer.android.com/reference/android/app/AlarmManager) um Tasks zu bestimmter Zeit zu senden</li><li>Implementieren der Features für die *Automatsiche Antwort* Funktion</li></ul> |
| falls Zeit vorhanden | Automatisches Versenden WhatsApp Nachrichten |  |
