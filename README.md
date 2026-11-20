# Extra Enchants

Fünf neue Verzauberungen: Schnelligkeit (Hosen), Heilung (Harnisch), Doppelsprung (Schuhe) sowie Nachtsicht und Sättigung (Helm).
Dazu die Elytra-Rüstung, ein Diamantharnisch mit Elytren und Raketenschub.

Teil der Mod-Reihe *Quality of Mine*. Jedes Feature des früheren Gesamt-Mods ist jetzt eine eigene Mod, die sich einzeln
installieren lässt. Alle Mods der Reihe laufen auf Einzelspielerwelten und auf Mehrspieler-Servern und funktionieren
unabhängig voneinander.

| Mod | Projekt | Inhalt |
|---|---|---|
| Extra Enchants (diese Mod) | `extra-enchants` | Fünf neue Verzauberungen und die Elytra-Rüstung |
| Tidy Inventory | `tidy-inventory` | Items nicht aufheben und Mülleimer im Inventar |
| Wide Hotbar | `wide-hotbar` | Item-Leiste mit 11 Plätzen |
| Sleep Lapse | `sleep-lapse` | Zeitraffer beim Schlafen |
| Breeding Break | `breeding-break` | Ruhepause nach der Paarung |
| Pocket Pals | `pocket-pals` | Bälle zum Fangen von Tieren, Gürtel dafür und Köcher für Pfeile |

## Installation

Voraussetzungen:

- Windows (das Installationsskript läuft nur dort, unter macOS und Linux musst du Fabric und die Mod von Hand installieren)
- Minecraft Java Edition mit dem offiziellen Launcher
- Java 25 (eine JRE reicht für die Installation)

Das Repository ist privat. Die Installationsdateien `installieren.cmd` und `extra-enchants-2.1.0.jar` werden
direkt weitergegeben, zum Beispiel als ZIP-Datei.

1. **Sichere deine Welten.** Kopiere dazu den Ordner `%APPDATA%\.minecraft\saves` an einen anderen Ort.
2. Schließe Minecraft und den Launcher.
3. Entpacke die ZIP-Datei in einen beliebigen Ordner. `installieren.cmd` und die JAR müssen im selben Ordner liegen.
4. Doppelklicke `installieren.cmd`. Das Skript lädt Fabric selbst herunter. Windows kann dabei vor dem
   unbekannten Skript warnen, dann auf *Weitere Informationen > Trotzdem ausführen* klicken.
5. Starte den Launcher, wähle das Profil **fabric-loader-26.3** und klicke auf **Spielen**.

Wer das Repository geklont hat, baut die JAR mit `gradlew build`. Das Skript findet sie dann in `build\libs`.

Das Skript installiert Fabric und kopiert die Mod nach `%APPDATA%\.minecraft\mods`. Die Fabric API steckt
bereits in der Mod und muss nicht extra installiert werden. Dein normales Vanilla-Profil bleibt erhalten.
Mehrere Mods der Reihe kannst du nebeneinander installieren, führe dazu das Skript jeder Mod einmal aus.

### Umstieg vom alten Gesamt-Mod

Den früheren Gesamt-Mod *Quality of Mine* (`quality-of-mine-1.3.0.jar`) und diese Mod darfst du **nicht gleichzeitig** installieren.
Beide würden dieselben Inhalte anmelden. Die Mod meldet das beim Start mit einer verständlichen Fehlermeldung, statt abzustürzen.

- **Auf dem Rechner:** `installieren.cmd` löscht die alte JAR selbst aus dem Ordner `mods`.
- **Auf dem Server:** Lösche `quality-of-mine-*.jar` im Ordner `mods` von Hand, bevor du die neue JAR hineinlegst.
- **Deine Welten:** Alle Kennungen bleiben unverändert (`qualityofmine:...`). Bestehende Welten behalten deshalb ihre Daten,
  solange die Mod mit dem jeweiligen Inhalt installiert ist. Fehlt eine Mod der Reihe, fehlt ihr Inhalt in der Welt, siehe unten.
- **Danach:** Jede Mod lässt sich einzeln aktualisieren, ohne die anderen anzufassen. Auf einem Server müssen Spieler und Server
  pro Mod dieselbe Version haben.

### Update und Deinstallation

- **Update:** Neue `installieren.cmd` und neue JAR in einen Ordner legen und das Skript erneut ausführen. Die alte Version dieser Mod wird ersetzt.
  Wer selbst baut, führt vorher `gradlew build` aus.
- **Deinstallation:** `%APPDATA%\.minecraft\mods\extra-enchants-2.1.0.jar` löschen.

Ohne die Mod verschwindet aus einer Welt alles, was zu ihr gehört: die Verzauberungen Schnelligkeit, Heilung, Doppelsprung, Nachtsicht und Sättigung
sowie alle Elytra-Rüstungen samt ihren Verzauberungen. Lege sie vorher ab, zum Beispiel in eine Truhe, wenn du die Mod später wieder installieren willst.

## Server

Die Mod Extra Enchants läuft auf einem Fabric-Server für Minecraft 26.3. **Sie muss auf dem Server und bei allen Spielern installiert
sein, und zwar in derselben Version.** Ein Spieler ohne sie würde sich mit dem
Server nicht verstehen. Der Server prüft deshalb beim Beitritt die Version und lehnt andere Spieler mit einer
Meldung ab, die beide Versionen nennt.

1. **Fabric-Server einrichten.** Voraussetzung ist Java 25. Lade den Fabric Installer von <https://fabricmc.net/use/server/>
   und installiere in den Serverordner (ersetze den Dateinamen durch den des Installers):

   ```cmd
   java -jar fabric-installer-1.1.2.jar server -mcversion 26.3 -loader 0.20.1 -downloadMinecraft
   ```

2. Starte den Server einmal mit `java -jar fabric-server-launch.jar nogui` und stimme der Mojang-EULA in `eula.txt` zu
   (`eula=true`). Das ist deine Entscheidung als Betreiber.
3. Kopiere `extra-enchants-2.1.0.jar` in den Ordner `mods` des Servers. Die Fabric API steckt bereits im Mod.
4. Starte den Server neu. Deine Mitspieler installieren den Mod wie oben beschrieben mit `installieren.cmd`.

Das Update läuft genauso: alte JAR im Ordner `mods` des Servers durch die neue ersetzen und alle Mitspieler
aktualisieren lassen.

Das ändert sich auf einem Server:

- **Doppelsprung und Schnelligkeit** rechnet der Client des jeweiligen Spielers, wie auch im Originalspiel Sprünge und
  Bewegung. Der Server übernimmt sie.
- **Raketenschub und Raketenstart** prüft und zählt der Server. Der Client meldet nur, dass die Sprungtaste im Flug gedrückt wurde.
- **Nachtsicht-Einstellung** gehört jedem Spieler einzeln und steht in dessen Spielerdaten. Sie bleibt nach dem Tod und
  nach einem Neustart des Servers erhalten.
- **Server ohne die Mod:** Wer mit installierter Mod auf einen fremden Server ohne sie geht, sollte sie dort nicht nutzen. Dafür nimmst du das normale Vanilla-Profil des Launchers.

## Features

### Verzauberung Schnelligkeit

Für Hosen, Stufen I bis III. Beim Sprinten bist du 10, 20 oder 30 % schneller, beim normalen Gehen ändert sich nichts.
Sie lässt sich mit allen anderen Hosen-Verzauberungen kombinieren, auch mit Doppelsprung auf den Schuhen.

Das Gewicht beträgt 2 (zum Vergleich: Behutsamkeit 1, Haltbarkeit 5).

| Quelle | Verhalten |
|---|---|
| Zaubertisch | Das Gewicht gilt. Auf Stufe 30 mit Diamanthosen in rund 40 % der Verzauberungen, meist Stufe II, seltener III. Stufe I kommt nur an schwächeren Tischen vor. |
| Angeln | Das Gewicht gilt, nur als Schatz in Form eines Buchs. |
| Bibliothekar | Das Gewicht gilt hier nicht: Das Spiel wählt aus allen handelbaren Verzauberungen gleichmäßig aus. |

### Verzauberung Heilung

Für den Harnisch, Stufen I bis III. Sie gibt unabhängig vom Hunger ein halbes Herz zurück:

| Stufe | Ein halbes Herz alle |
|---|---|
| I | 60 Sekunden |
| II | 40 Sekunden |
| III | 20 Sekunden |

Gewicht und Kosten sind dieselben wie bei Schnelligkeit, sie ist also genauso häufig. Am Zaubertisch auf Stufe 30
mit einem Diamantharnisch erscheint sie in rund 32 % der Verzauberungen, meist Stufe II.

### Verzauberung Doppelsprung

Für Schuhe, nur eine Stufe. In der Luft springst du durch erneutes Drücken der Sprungtaste ein zweites Mal.
Damit kommst du gut 2,5 Blöcke hoch, also über ein 2 Blöcke hohes Hindernis. Nach der Landung ist der Sprung wieder frei.

- Am besten drückst du am höchsten Punkt des ersten Sprungs. Der Fallschaden zählt erst ab dem zweiten Sprung.
- Auch nach dem Herunterlaufen von einer Kante kannst du einmal in der Luft springen.
- Mit Elytren öffnet das erste Drücken in der Luft den Doppelsprung, erst das nächste die Elytren.
- Im Kreativ-Modus, im Wasser, an Leitern und beim Reiten gibt es keinen Doppelsprung.
- Das Gewicht beträgt 1 wie bei Behutsamkeit, am Zaubertisch auf Stufe 30 mit Diamantschuhen in rund 13 % der Verzauberungen.

### Verzauberung Nachtsicht

Für den Helm, nur eine Stufe. Sie wirkt wie der Nachtsichttrank, nur ohne Partikel und ohne Symbol.

| Taste (Standard) | Wirkung |
|---|---|
| `N` | Nachtsicht ein- oder ausschalten, solange der Helm getragen wird |

- Die Einstellung gilt pro Spieler und Welt und bleibt auch nach dem Tod erhalten.
- Ein getrunkener Nachtsichttrank läuft beim Ausschalten weiter.
- Nach Milch kommt die Nachtsicht sofort zurück.
- Gewicht 2 wie Schnelligkeit, am Zaubertisch auf Stufe 30 mit einem Diamanthelm in rund 19 % der Verzauberungen.

### Verzauberung Sättigung

Für den Helm, Stufen I bis III. Laufen, Springen, Kämpfen und Heilen zehren weniger an dir: Der Verbrauch sinkt
um 10, 25 bzw. 50 %. Das gilt für die versteckten Sättigungspunkte und danach für die Hungerleiste.
Mit Stufe III hält eine Mahlzeit also doppelt so lange.

Gewicht und Kosten sind dieselben wie bei Schnelligkeit. Am Zaubertisch auf Stufe 30 mit einem Diamanthelm erscheint
sie in rund 19 % der Verzauberungen, meist Stufe II. Mit Nachtsicht lässt sie sich auf demselben Helm kombinieren.

### Unendlichkeit und Reparatur auf dem Bogen

Die beiden Verzauberungen des Originalspiels lassen sich auf dem Bogen kombinieren. Das gilt in Amboss, Zaubertisch
und überall sonst, wo das Spiel Verzauberungen zusammenführt. Alle anderen Unverträglichkeiten bleiben, wie sie sind,
etwa Schärfe und Bann der Toten.

- Umgesetzt ist das als Datei `data/minecraft/tags/enchantment/exclusive_set/bow.json`, die die Ausschlussliste des Bogens leert.
- Bereits verzauberte Bögen bleiben unverändert, es entsteht nur eine neue Möglichkeit.

### Elytra-Rüstung

Ein Diamantharnisch mit Elytren: Sie schützt wie der Harnisch, und du gleitest damit wie mit Elytren.
Mit geladenen Feuerwerksraketen gibt sie im Flug Schub.

**Herstellen.** Lege im Amboss den Diamantharnisch links und die Elytren rechts hinein. Das kostet 5 Erfahrungsstufen,
dazu wie im Spiel die Kosten früherer Ambossarbeiten an beiden Teilen.

- Die Rüstung übernimmt alles vom Harnisch: Verzauberungen, Namen und Rüstungsbesatz. Die Verzauberungen der Elytren
  kommen dazu, bei doppelten zählt die höhere Stufe.
- Die Haltbarkeit ist die Summe beider Teile, 528 + 432 = 960. Treffer und Fliegen zehren am selben Balken, der
  Verschleiß beider Teile wird übernommen.
- Repariert wird mit Diamanten oder Phantomhaut, oder im Amboss mit einer zweiten Elytra-Rüstung.
- Verzaubern lässt sie sich wie ein Harnisch, also auch mit Heilung.
- Bei einer Haltbarkeit von 1 kannst du wie bei Elytren nicht mehr gleiten. Treffer können sie aber wie jede Rüstung zerbrechen.

**Netherit.** Am Schmiedetisch wird sie mit Netherit-Aufwertung und Netheritbarren zur Netherit-Elytra-Rüstung
(Haltbarkeit 592 + 432 = 1024). Sie schützt wie ein Netheritharnisch und verbrennt nicht.

**Raketenschub.** Nimm im Inventar Feuerwerksraketen auf und klicke damit auf die Rüstung, wie beim Bündel:

| Klick | Wirkung |
|---|---|
| Rechtsklick | Eine Rakete hineinlegen |
| Linksklick | So viele Raketen hineinlegen, wie passen |

- Eine Rakete gibt 48 Schübe, voll sind 2 Raketen, also 96 Schübe. Eine weitere Rakete passt erst hinein, wenn die
  Hälfte verbraucht ist, so geht keine Ladung verloren. Jede Rakete zählt gleich, auch eine mit Feuerwerksstern.
- Im Flug gibt jedes erneute Drücken der Sprungtaste einen Schub. Er zieht 20 % stärker als eine Rakete in der Hand und
  brennt rund 5 Sekunden länger, also 6 bis 6,5 Sekunden, ohne Explosion.
- Ein orangefarbener Balken über dem Haltbarkeitsbalken zeigt die Ladung. Der Tooltip nennt die genaue Zahl.
- Solange sie geladen ist, trägst du zwei Raketen auf dem Rücken, auch das Symbol zeigt sie dann.
- Im Kreativ-Modus verbrauchen die Schübe keine Ladung.
- Die Zahl der Schübe pro Rakete steht als `BOOSTS_PER_ROCKET` in `RocketCharge`, Stärke und Dauer stehen als
  `SPEED_FACTOR` und `FLIGHT_DURATION` in `ArmorRocket`.

**Raketenstart.** Mit geladener Rüstung startest du per Taste direkt in den Flug, ohne Kette aus Sprung, Doppelsprung und Flügel öffnen.

| Taste (Standard) | Wirkung |
|---|---|
| `V` am Boden | Kräftiger Absprung, Flügel auf und Schub |
| `V` im Fall | Sofort Flügel auf und Schub |
| `V` im Gleitflug | Schub wie mit der Sprungtaste |

- Die Taste lässt sich unter *Optionen > Steuerung* ändern, Kategorie Extra Enchants.
- Jeder Start kostet eine Ladung. Im Wasser und beim Reiten gibt es keinen Start.
- Die Richtung bestimmt dein Blick: Schau beim Start nach oben, um schnell an Höhe zu gewinnen.

## Entwicklung

Der Build läuft über den mitgelieferten Gradle Wrapper. Das Fabric-Build-Plugin Loom gibt es nur für Gradle,
eine eigene Gradle-Installation ist nicht nötig. Benötigt wird JDK 25.

```cmd
gradlew build
gradlew test
gradlew runClient
gradlew runClientGameTest
gradlew productionClientTest
```

| Befehl | Zweck |
|---|---|
| `build` | Baut `build\libs\extra-enchants-2.1.0.jar` und führt die Unit-Tests aus |
| `test` | Unit-Tests der Logik |
| `runClient` | Startet das Spiel mit dem Mod in einem eigenen Testordner |
| `runClientGameTest` | Spielt Extra Enchants automatisch im echten Spiel durch, anschließend gegen einen echten dedizierten Server (Beitritt mit Versionsabfrage) |
| `runServer` | Startet einen dedizierten Server mit dem Mod in `run`, zum Beispiel für manuelle Tests mit `runClient` als zweitem Spieler |
| `productionClientTest` | Derselbe Test mit dem fertigen JAR, wie bei einer echten Installation |

Für IDE-Projektdateien gibt es `gradlew eclipse` und `gradlew genSources` (dekompilierter Spielcode).

`productionClientTest` stürzte auf dem Entwicklungsrechner beim Start gelegentlich nativ ab (`0xC0000005`), auch ganz ohne Mod. Die Ursache ist nicht geklärt. In dem Fall den Test mit `--rerun` wiederholen.

### Neues Feature hinzufügen

1. Neues Paket unter `src/main/java/de/qualityofmine/extraenchants/feature/` anlegen.
2. Eine Klasse mit `Feature` anlegen und in `ExtraEnchants` eintragen. Client-Anteile kommen nach `src/client/java` und in `ExtraEnchantsClient`.
3. Eingriffe ins Spiel als möglichst dünne Mixins unter `mixin` anlegen und in `extraenchants.mixins.json` bzw. `extraenchants.client.mixins.json` eintragen. Die Logik gehört in die Feature-Klassen.
4. Texte in `assets/qualityofmine/lang/de_de.json` und `en_us.json` ergänzen.

### Neue Minecraft-Version

Die Versionen stehen in `gradle.properties` und `installieren.cmd`, aktuelle Werte unter <https://fabricmc.net/develop>.
Danach zeigt `gradlew productionClientTest`, ob noch alles funktioniert.

### Neue Version weitergeben

1. Version in `gradle.properties` und `MOD_VERSION` in `installieren.cmd` anpassen. Auch die Dateinamen in dieser README stehen mit Version.
2. `paket-erstellen.cmd` ausführen. Das Skript baut den Mod komplett neu (`gradlew clean build`, inklusive Unit-Tests),
   prüft, ob die Versionen in `gradle.properties` und `installieren.cmd` zusammenpassen, und packt die JAR
   und `installieren.cmd` in `dist\extra-enchants-<Version>.zip`.
3. Diese ZIP-Datei weitergeben.

## Lizenz

[MIT](LICENSE)
