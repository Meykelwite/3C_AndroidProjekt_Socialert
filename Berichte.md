# Wöchentliche Berichte / Resümees

## Woche vom 9. Juni 2021

### Was ist gut gelaufen?

Layout nun vollständig. Vorraussichtlich werden wir fertig.

### Welche Arbeitspakete wurden erledigt?

* Dialog, der aufgerufen wird, wenn der FloatingActionButton betätigt wird (neuer Task)

### Welche Probleme sind aufgetreten?

Die Arbeitspakete 
* Automatisch SMS versenden mit Location und
* Anbindung an Gmail-API

wurden nicht fertiggestellt.

### Wie liegt die Gruppe im Zeitplan?

siehe Probleme. Abweichungen vom Plan, aber wir werden es schaffen.

### Was wird nächste Woche umgesetzt

Die folgenden Arbeitspakete:

* Automatisch SMS versenden mit Location fertigstellen
* Anbindung an Gmail-API fertigstellen
* Alarm Manager
* Automatisch Antworten für SMS
* Notification Service

***

## Woche vom 2. Juni 2021

### Was ist gut gelaufen?

Keine allzu großen Verzüge im Zeitplan.

### Welche Arbeitspakete wurden erledigt?

* Tabs
* ListView (für die Tasks) und Adapter

### Welche Probleme sind aufgetreten?

Die ganze Projektstruktur wurde geändert, was nicht eingeplant war und daher mehr Zeit als erwartet in Anspruch genommen hat. Auch die Implementierung, um die Fragments refreshen zu können, funktionierte nicht auf Anhieb.

### Wie liegt die Gruppe im Zeitplan?

Kleine Abweichungen vom Plan.

### Was wird nächste Woche umgesetzt

Die folgenden Arbeitspakete:

* Dialog, der aufgerufen wird, wenn der FloatingActionButton betätigt wird (neuer Task). Dabei soll u.a. auch auf die gespeicherten Kontakte zugegriffen werden.
* Automatisch SMS versenden mit Location
* Anbindung an Gmail-API

***

## Woche vom 26. Mai 2021

### Was ist gut gelaufen?

Der NavigationDrawer konnte bis auf ein paar Probleme mit der ActionBar relativ schnell umgesetzt werden. Bei den Preferences ist alles gut gelaufen.

### Welche Arbeitspakete wurden erledigt?

* Lokales Repository mit dem Remote Repository verbinden
* Fragments
* Layout
* Preferences

### Welche Probleme sind aufgetreten?

Probleme bereiteten uns vor allem die Master-Detail-Ansichten in den Fragments *Task planen* und *Automatische Antwort*. Das Problem konnten wir wie folgt lösen.

MainActivity#onSelectionChanged
```java
    @Override
    public void onSelectionChanged(String task) {
        this.selectedTask = task;
        if (showRight) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (isScheduleTask(navHostFragment)) {
                    ScheduleTaskFragment scheduleTaskFragment = (ScheduleTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    TaskDetailFragment taskDetailFragment = (TaskDetailFragment) scheduleTaskFragment.getChildFragmentManager().getFragments().get(1); // only way to get the TaskDetailFragment??
                    taskDetailFragment.show(task);
                } else {
                    AutoReplyTaskFragment autoReplyTaskFragment = (AutoReplyTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    TaskDetailFragment taskDetailFragment = (TaskDetailFragment) autoReplyTaskFragment.getChildFragmentManager().getFragments().get(1);
                    taskDetailFragment.show(task);
                }
        } else {
            startRightActivity(task);
        }
    }
```

### Wie liegt die Gruppe im Zeitplan?

Es läuft alles planmäßig.

### Was wird nächste Woche umgesetzt

Die folgenden Arbeitspakete:

* Tabs
* ListView (für die Tasks) und Adapter
* Activity, die aufgerufen wird wenn der FloatingActionButton betätigt wird (neuer Task)
