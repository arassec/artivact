import{_ as e,c as a,o as s,a2 as n}from"./chunks/framework.DSgG5TdZ.js";const m=JSON.parse('{"title":"Installation als Web-Server","description":"","frontmatter":{},"headers":[],"relativePath":"de/quick-start/server-installation/installation.md","filePath":"de/quick-start/server-installation/installation.md"}'),t={name:"de/quick-start/server-installation/installation.md"},i=n(`<h1 id="installation-als-web-server" tabindex="-1">Installation als Web-Server <a class="header-anchor" href="#installation-als-web-server" aria-label="Permalink to &quot;Installation als Web-Server&quot;">​</a></h1><h2 id="installation" tabindex="-1">Installation <a class="header-anchor" href="#installation" aria-label="Permalink to &quot;Installation&quot;">​</a></h2><p>Artivact ist eine Java-Anwendung, die das Open-Source Framework <a href="https://spring.io/" target="_blank" rel="noreferrer">Spring</a> verwendet. Als solche wird sie als self-contained JAR-Datei ausgeliefert.</p><p>Die aktuellste Version kann von der Github-Projektseite geladen werden: <a href="https://github.com/arassec/artivact/releases/latest" target="_blank" rel="noreferrer">Artivact Releases</a>.</p><p>Die relevante Datei enthält &#39;server&#39; im Dateinamen, z.B. <code>artivact-server-v0.0.0.jar</code></p><h2 id="start" tabindex="-1">Start <a class="header-anchor" href="#start" aria-label="Permalink to &quot;Start&quot;">​</a></h2><p>Ein Java JRE oder JDK muss bereits auf dem System installiert sein um die Anwendung zu starten.</p><p>Zum start kann dann einfach der folgende Befehl auf der Kommandozeile ausgeführt werden:</p><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span>$&gt; java -jar artivact-server-v0.0.0.jar</span></span></code></pre></div><div class="info custom-block"><p class="custom-block-title">INFO</p><p>In dem Verzeichnis, welches die JAR-Datei einhält, wird das Standard-Projektverzeichnis <code>.avdata</code> angelegt, falls nicht anders konfiguriert.</p></div><p>Während des ersten Starts der Anwendung wird das initialize Administratorkonto <code>admin</code> angelegt. Das Passwort kann in der Logdatei der Anwendung gefunden werden. Dies sieht z.B. folgendermaßen aus:</p><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span>##############################################################</span></span>
<span class="line"><span>Initial user created: admin / ebcfd5c6</span></span>
<span class="line"><span>##############################################################</span></span></code></pre></div><h2 id="linux-system-daemon" tabindex="-1">Linux System Daemon <a class="header-anchor" href="#linux-system-daemon" aria-label="Permalink to &quot;Linux System Daemon&quot;">​</a></h2><p>Soll Artivact auf einem Linux-Server betrieben werden, bietet es sich an die Anwendung als systemd-Dienst zu betreiben.</p><p>Dafür muss zunächst ein Nutzer, unter dem die Anwendung laufen soll, erzeugt werden. Danach kann ein Verzeichnis für die Anwendung erstellt und die JAR-Datei dorthin kopiert werden:</p><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span>$&gt; sudo useradd artivact</span></span>
<span class="line"><span>$&gt; sudo mkdir /opt/artivact-server</span></span>
<span class="line"><span>$&gt; sudo mv artivact-server-v0.0.0.jar /opt/artivact-server/</span></span>
<span class="line"><span>$&gt; sudo chown -R artivact /opt/artivact-server/</span></span></code></pre></div><p>Als Nächstes muss die folgende Datei im System abgelegt werden: <code>/etc/systemd/system/artivact-server.service</code></p><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code"><code><span class="line"><span>[Unit]</span></span>
<span class="line"><span>Description=Artivact Server</span></span>
<span class="line"><span>After=syslog.target</span></span>
<span class="line"><span></span></span>
<span class="line"><span>[Service]</span></span>
<span class="line"><span>User=artivact</span></span>
<span class="line"><span>ExecStart=/opt/artivact-server/artivact-server-v0.0.0.jar</span></span>
<span class="line"><span>SuccessExitStatus=143</span></span>
<span class="line"><span></span></span>
<span class="line"><span>[Install]</span></span>
<span class="line"><span>WantedBy=multi-user.target</span></span></code></pre></div><p>Jetzt kann Artivact mit systemd-Kommandos verwaltet werden, z.B. mit diesen:</p><ul><li>Start der Anwendung: <code>$&gt; sudo systemctl start artivact-server</code></li><li>Stop der Anwendung: <code>$&gt; sudo systemctl stop artivact-server</code></li><li>Bootsicher machen: <code>$&gt; sudo systemctl enable artivact-server</code></li></ul>`,20),r=[i];function l(d,p,o,c,u,v){return s(),a("div",null,r)}const g=e(t,[["render",l]]);export{m as __pageData,g as default};