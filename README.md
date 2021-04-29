<h2 align="center">Autosweeper - RP2020</h2>
Autosweeper je variace na známou hru jménem Minesweeper, ve které se hledají miny.
Hráč dostane libovolně velké dvourozměrné pole čtverečků, pod kterými se můžou skrývat miny. 
Cílem hry je postupně odhalovat čtverečky pod kterými miny nejsou. Při každém odhalení je 
hráči ukázáno číslo, které značí počet min v okolních osmi políčkách. Z těchto informací 
musí hráč hádat, jaké políčka může odhalit.

Součástí programu je automat, který hru umí řešit. Hráč si může vybrat mezi třemi herními módy:
samostatný, s pomocí a automatický. Když si hráč vybere samostatnou hru, tak se automat do hry
nijak nezapojuje. Při hře s pomocí může hráč kliknout na tlačítko "analyzovat", které automat 
spustí a ukáže hráči jaké políčka jsou vyhodnocena jako nebezpečná. V automatickém módu automat 
řeší hru celou sám.

Při jakékoliv hře bude také běžet časovač, aby hráč mohl vidět své zlepšení, či zhoršení. 
Jestliže hru bude řešit jen automat, tak bude časovač běžet jen při jeho "přemýšlení" neboli 
vyhodnocování nebezpečnosti políček.

#### Instalace
1. Zkompiluje kód, nebo stáhněte `.jar` a složku `resources` v releases
2. Spusťte `.jar` ve stejné složce jako jsou `resources`
3. Enjoy!
