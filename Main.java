import extensions.File;
import extensions.CSVFile;

/**
 * Microsoft World Conquest ft. Julien Baste
 * Le jeu RPG dans le Terminal absolument magnifique.
 * Il vous faudra une RTX 4070 et un processeur Ryzen 7950X au minimum pour l'exécuter.
 * Plus sérieusement, un bon processeur peut éviter des problèmes d'affichage quand ce dernier a trop de choses à afficher en simultané sur des threads différents.
 * @author Thomas Gysemans, Manon Leclercq, S1-C, 2022
 */
class Main extends Program {
    final String VERSION = "alpha";
    final String[] DAYS = new String[]{"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"};
    final int GUI_HORIZONTAL_MARGIN = 2;
    final int GUI_VERTICAL_MARGIN = 1;

    // Les valeurs numériques arbitraires associées aux flèches du clavier.
    // Nous avons trouver certaines valeurs en regardant le code source de iJava ;)
    final int TOP_ARROW_KEY = 17;
    final int BOTTOM_ARROW_KEY = 18;
    final int LEFT_ARROW_KEY = 19;
    final int RIGHT_ARROW_KEY = 20;
    final int ENTER_KEY = 13;
    final int CONFIRMATION_KEY = 111;
    final int DENY_KEY = 110;

    final String WALKING_COMMAND = "Marcher";
    final String INTERACTIVE_COMMAND = "Interaction";
    final String SYSTEM_COMMAND = "Système";

    // Chaque commande peut être personnalisée,
    // donc si on ne peut pas les reconnaître de manière définitive avec la touche associée,
    // alors il nous faut un identifiant unique.
    // Il faut donc bien faire attention à ce que les noms correspondent dans le fichier CSV.
    final String KEY_INTERACT = "INTERACT";
    final String KEY_CONTACT = "CONTACT";
    final String KEY_START_LESSON = "START_LESSON";
    final String KEY_QUIT = "QUIT";
    final String KEY_WALK_TOP = "TOP";
    final String KEY_WALK_RIGHT = "RIGHT";
    final String KEY_WALK_BOTTOM = "BOTTOM";
    final String KEY_WALK_LEFT = "LEFT";
    // Si on utilisait vraiment Java sans restrictions,
    // on aurait fait les choses autrement, et d'une meilleure façon...
    final int NUMBER_OF_WALKING_COMMANDS = 4;
    final int NUMBER_OF_INTERACTION_COMMANDS = 3;
    final int NUMBER_OF_SYSTEM_COMMANDS = 1;

    // Un booléan pour savoir quand l'utilisateur a terminé de se déplacer ou d'intéragir.
    // Une valeur à `true` stoppe la saisie si elle est déjà démarrée, et arrête le programme.
    boolean finishedTyping = false;

    Page page = Page.MENU; // la page actuelle
    int selectedMenu = 1; // par défaut, le joueur va sélectionner le premier élément du menu principal
    int menuPosY = 0;
    final int MENU_ITEMS_NUMBER = 4; // 4 choix possibles dans le menu

    int selectedCommand = 1; // la première commande sélectionnée dans la page des commandes
    int selectedCommandPosY = 0;
    boolean isWaitingForKeyInput = false; // true si on attend que l'utilisateur entre une nouvelle touche pour réassigner une commande 

    final String CREDITS_TITLE_PATH = "../assets/ascii/credits-title.txt";
    final String CREDITS_PATH = "../assets/credits.csv";
    final String GAME_MAIN_TITLE_PATH = "../assets/ascii/main-title.txt";
    final String COMMANDS_TITLE_PATH = "../assets/ascii/commands-title.txt";
    final String MAPS_PATH = "../assets/maps/"; // le fichier contenant toutes les cartes
    final String COLORS_PATH = "../assets/0-colors.csv"; // le fichier contenant toutes les couleurs
    final String IMPORTANT_CHARACTERS_PATH = "../assets/0-important-characters.csv"; // les personnages importants de l'histoire dont il faut connaître la couleur
    final String HELP_PATH = "../assets/0-baste-help.csv"; // l'aide du jeu, qu'on fait passé comme venant de Baste
    final String TELEPORTATIONS_PATH = "../assets/0-teleportations.csv"; // le fichier contenant toutes les passerelles entre les maps
    final String DIALOGS_PATH = "../assets/0-dialogs.csv"; // le fichier contenant tous les dialogues des couleurs interactives
    final String COMMANDS_PATH = "../assets/0-commands.csv"; // le fichier contenant toutes les commandes par défaut
    final String TVINFO_PATH = "../assets/0-tv.csv"; // le fichier contenant toutes les infos diffusées par la télé de la cellule du joueur
    final String ENGLISH_LESSONS_PATH = "../assets/lessons/english.csv";
    final String FRENCH_LESSONS_PATH = "../assets/lessons/french.csv";
    final String MATHS_LESSONS_PATH = "../assets/lessons/maths.csv";
    final String HISTORY_LESSONS_PATH = "../assets/lessons/history.csv";
    final String PRISONERS_PATH = "../assets/0-prisoners.csv";
    final String RICK_PATH = "../assets/ascii/rick.txt";
    final String SECRETS_PATH = "../assets/0-secrets.csv";
    final String PIXEL = "  "; // En réalité, un pixel est deux espaces dont le fond est coloré avec ANSI
    final int PIXEL_SIZE = length(PIXEL); // On aura besoin de cette constante dans le calcul de mouvement vers la droite/gauche
    String currentMap = "bibliotheque"; // la carte actuellement affichée
    int playerX = 18; // la position en X du joueur, par défaut ça devrait être 18 dans la bibliothèque
    int playerY = 9; // la position en Y du joueur, par défaut ça devrait 9 dans la bibliothèque
    int nearestInteractiveCell = -1; // obtiens la valeur de la couleur si le joueur est proche d'une cellule interactive (redéfinie quand on utilise `hasInteractiveCellNearPlayer` dans `displayPlayer`)

    // On veut stocker les informations au préalable
    // Pour éviter de charger les fichiers, et de les recharger quand nécessaire,
    // on charge tout une seule fois avant le début du jeu.
    // Pour notre plus grand malheur, ArrayList n'est pas autorisée (askip)
    Color[] COLORS;
    Dialog[] DIALOGS;
    Map[] MAPS; // lol avant on avait int[][][] MAPS quel délire !
    Command[] COMMANDS;
    TVInfo[] TV_INFO;
    Credit[] CREDITS;
    Help[] HELP;
    Secret[] SECRETS;
    final int SECRETS_TRESHOLD = 5;
    Lesson[] ENGLISH_LESSONS;
    Lesson[] FRENCH_LESSONS;
    Lesson[] MATHS_LESSONS;
    Lesson[] HISTORY_LESSONS;
    String[] PRISONERS_DIALOGS;
    int selectedLessonAnswer = 1;
    int selectedLessonAnswerPosY = 0;

    Dialog[] currentDialogs = null;
    int currentGroupIndex = 0; // l'indice du message du groupe actuel dans `currentDialogs`
    Help[] currentHelp = null;
    int currentHelpIndex = 0;

    final int DAY_DELAY = 2*60; // nombre de secondes (in-game) pour un jour
    final int HOUR_DELAY = DAY_DELAY/24; // délai pour une heure
    final int DEADLINE = 30; // si `day` atteint cette valeur, alors game over!
    int hour = 17; // l'heure actuelle (in-game)
    int day = 0; // le jour actuel (in-game)
    Thread time; // le thread séparé correspondant au temps
    
    // Pour contrôler le défilement des crédits, on a besoin de variables globales.
    // On utilise des fonctions lambda, et elles requièrent des variables finales ou définies dans un contexte supérieur.
    // On utilise également un Thread pour que l'on puisse quitter à tout moment, même si le défilement n'est pas fini.
    Thread creditsThread;
    int nDisplayedCredits = 0;
    String currentCategory = "";

    boolean game_started = false;

    // Toutes les variables globales liées à la progression du joueur
    // ----
    // Dans cet ordre spécifique
    boolean MET_BASTE = false;
    boolean KIDNAPPING = false;
    boolean COMMUNICATED_WITH_BASTE_FOR_THE_FIRST_TIME = false; // une fois dans la prison
    boolean DISCOVERED_CONTROL_PC = false;
    boolean HAS_LINUX = false; // la clé USB donnée par Baste
    boolean HAS_USB_KEY = false; // la clé USB donnée par Mathieu
    boolean HAS_PASSWORD = false;
    boolean HAS_COPIED_SECRET_FILES = false;
    boolean EVASION_STARTED = false;
    Thread evasionThread; // utilisé pour contrôler le timer lors de l'évasion
    int evasionRemainingTime = 5; // secondes

    // Variables de contrôle de l'état
    // ---
    boolean TIME_PASSING = true; // dans le prologue, le temps ne passe pas, car il y a une échéance
    boolean LOCK_KEYS = false;
    boolean WANTS_TO_SLEEP = false;
    boolean WAITING_CONFIRMATION_TO_END_THE_GAME = false;

    // Ces deux variables sont là pour écouter et recevoir l'input de l'utilisateur,
    // quand on veut qu'il écrive quelque chose (une commande BASH par exemple).
    boolean WAITING_FOR_USER_INPUT = false;
    String INPUT = ""; // on enregistre ce qu'il écrit ici
    int CURRENT_NUM_OF_LINES_IN_OUTPUT = 13;

    int PC_INDEX;
    int BASTE_INDEX;
    int MATHIEU_INDEX;
    int BED_INDEX;
    int PC_CONTROL_INDEX;
    int PRISONER_INDEX;
    int PRISONER_INDEX_IN_CLASSROOM;
    int GROUND_INDEX;
    int GUARD_INDEX;
    int CELL_INDEX;
    int ACCESS_TO_COURTYARD;
    int PRISON_DOOR_INDEX;
    int TV_INDEX; // l'indice de la couleur correspondant à la télé de la cellule, obtenu en lisant `0-tv.csv`
    int lastIndexOfTVInfo = 0; // l'indice de la dernière news affichée
    Lesson CURRENT_LESSON = null;
    boolean WAITING_FOR_ANSWER_TO_LESSON = false;
    int LAST_LESSON_INDEX = -1;

    // En ce qui concerne l'interpréteur Bash :
    final String BASE64_SECRET_CONTENT = "Tm9tLFByw6lub20sQ2l0YXRpb24sUm9sZQpHYXRlcyxCaWxsLCJTaSB2b3VzIHZvdWxleiB1bmUgY2l0YXRpb24sIHByZW5leiByZW5kZXotdm91cy4iLEZvbmRhdGV1cgpDZW5hLEpvaG4sIkFuZCBoaXMgbmFtZSBpcyAhIixCb3hldXIKQmFzdGUsSnVsaWVuLCJMZXMgb3JkaW5hdGV1cnMsIGMnZXN0IHN0dXBpZGUiLEFuY2llbi1Ew6l0ZW51Ck1hdGhpZXUsUGhpbGlwcGUsIkplIGNvbXByZW5kcyBwYXMgY2UgcXVpIGVzdCBudWwgYXZlYyBBY2Nlc3M6IHRvdXQgZXN0IGfDqW5pYWwgISBFbiBwbHVzIMOnYSBjb8O7dGUgcXVlIDEgMDAwIDAwMOKCrCIsR2FyZGUKbMOrb04sY3RyY3RyY3RyYMOtb04sIkplIHZhaXMgYXZvaXIgNi8yMCBhdSBEUyIsUMOocmUtTm/Dq2wKRGFtYXktR2xvcmlldXgsSGlwcG9seXRlLCJKJ2FpIG1pcyB1biBmaWNoaWVyIHRleHRlICd3b2xvbG8nIHN1ciBsZSBQQyBkZSBUaG9tYXMuIElsIGZhaXQgNiBHQiIsR2FyZGUKQm91aW4sSnVsaWVuLCJKJ2FpIGxlIG3Dqm1lIHByw6lub20gcXVlIG1vbiBpZMO0bGUiLEFuY2llbi1Ew6l0ZW51ClJvdXNzZWwsTWF0dGhpYXMsIlF1J2VzdC1jZSBxdWUgamUgZm91cyBsw6A/IixEw6ltaXNzaW9ubsOpClNvY290YSxDb3JlbnRpbiwiUXVpIHByb3Rlc3RlIGVzdCB1biBlbm5lbWksIHF1aSBzJ29wcG9zZSBlc3QgdW4gY2FkYXZyZSIsRGljdGF0ZXVyCkdvbWV6LFRob21hcywiTW9uIGF2b2NhdCBtJ2EgaW50ZXJkaXQgZGUgZGl2dWxndWVyIG1vbiBpZMOpZSBkZSBwaHJhc2UgZCdhY2Nyb2NoZSIsQW5jaWVuLUTDqXRlbnUKRGVkb24sQWxleGFuZHJlLCJmb3J0IERldHJpY2sgYSBwcm9iYWJsZW1lbnQgdGVybWluw6kgdW5lIHBhcnRpZSBkZXMgdHJhdmF1eCBkZSBsJ3VuaXTDqSA3MzEgbWFpcyBjJ2VzdCBldXggbGVzIHNhdXZldXJzLCBjb21iaWVuIGQnYXV0cmVzIG1lbnNvbmdlcyBsZSBjb25zZWlsIGEtdC1pbCByw6lwYW5kdSA/IixQeXJvbWFuZQpJc2thayxBbGltLCJKJ2VuIGFpIG1hcnJlIGRlIGNlIGJvdWxvdC4uLiBKZSB2ZXV4IHJlbnRyZXIgY2hleiBtb2kiLEdhcmRlCk1hcmxhcmQsU2FzaGEsIkplIHN1aXMgZW50cmFpbiBkZSBmYWlyZSB1bmUgaGlzdG9pcmUgc3VyIGRlcyB2aWV1eCBxdWkgam91ZW50IGF1IHNjcmFiYmxlIHBvdXIgdW4gZGV2b2lyIGltcG9ydGFudCIsQW5pbWF0ZXVyClRvdXJuZXVyLEF5bWVyaSwiVm91cyBzYXZleiBxdW9pPyBFaCBiYWggZmV1ciAhIixEYW5zZXVyCkJsb3QsTWF4aW1lLCJBbW9ndXMiLEFuY2llbi1Ew6l0ZW51Cldvem5pYWssSsOpcsO0bWUsIlZpdmUgUHl0aG9uICEiLEdhcmRlCkJvdXJkZWF1LFRvbSwiVmVuZXogb24gam91ZSDDoCBGYWxsIEd1eXMiLEFuY2llbi1Ew6l0ZW51CkRlbW9yeSxMw6lhLCJXaG8gbWFrZXMgdGhlIG1hbGluIGZhbGxzIGluIHRoZSByYXZpbiIsR2FyZGUKQmFuc2UsQW50b2luZSwiSmUgc3VpcyBwYXMgZGFucyBsZSBGQ0MsIG1haXMgamUgZGlyYWlzIHBhcyBxdWUgamUgbGUgZMOpdGVzdGUgbm9uIHBsdXMuLi4iLEdhcmRlCkJvdW1hbnNvdXIsTWFuZWwsIkonYWkgcGVyZHUgbW9uIDUwLzUwIHN1ciBHZW5zaGluLCBtZSBwYXJsZXogcGFzIixHYXJkZQpGb3VnbmllLEFudGh5bWUsIkwnRVBTSSBjJ2VzdCBwYXMgZGUgbCdhcm5hcXVlIGplIHRyb3V2ZSIsR2FyZGUKRGVjcm9peCxDYW5kaWNlLCJKZSB2YWlzIHRvdXQgY2Fzc2VyIixHYXJkZQpNYW1hc2l0YSxMdWRtaWxsYSwiQydlc3QgcGFzIGNvbW1lIMOnYSBxdSdvbiBpbWFnaW5lIGxhIHZpZSwgcGFzIGNvbW1lIMOnYSIsR2FyZGUKTGVjbGVyY3EsTGF1cmVudCwiVnJvb29tIixHYXJkZQpEZWxhaGF5ZSxLaWxpYW4sIkFoIHp1dCwgYydlc3QgcmVwYXJ0aSBtb24ga2lraSIsR2FyZGUKw5Z6dGVwZSxZZWxpeiwiTGVzIGxhcGlucyBjJ2VzdCBtZXMgY29wYWlucyIsR2FyZGU=";
    final FileElement root = new FileElement(
        "/", "/", new FileElement[]{
            new FileElement("Bureau", "/Bureau", new FileElement[]{ new FileElement("answer_to_life.txt", "/Bureau/answer_to_life.txt", "42") }),
            new FileElement("Chien", "/Chien", new FileElement[]{ new FileElement("photo_chien1.jpg", "/Chien/photo_chien1.jpg"), new FileElement("photo_chien2.jpg", "/Chien/photo_chien2.jpg"), new FileElement("photo_chien3.png", "/Chien/photo_chien3.png") }),
            new FileElement("Famille", "/Famille", new FileElement[]{ new FileElement("photo_famille1.jpg", "/Famille/photo_famille1.jpg") }),
            new FileElement(".Microsoft", "/.Microsoft", BASE64_SECRET_CONTENT),
        }
    );
    /**
     * Quand l'utilisateur quitte le PC avec la commande BASH "exit",
     * nous devons vérifier s'il a copié les données confidentielles selon le plan.
     * Nous devons également relancer le temps et supprimer l'interface.
     */
    final Runnable onExit = () -> {
        TIME_PASSING=true;
        WAITING_FOR_USER_INPUT=false;
        clearDialogAndMessage();
        for (int i = 0; i < root.subElements.length; i++) {
            if (root.subElements[i].name.equals("USB-Mathieu")) {
                if (root.subElements[i].doesContainFile((FileElement element) -> {
                    if (element.type == Element.FILE) {
                        if (element.fileContent.startsWith(BASE64_SECRET_CONTENT.substring(0,10))) {
                            return true;
                        }
                    }
                    return false;
                })) {
                    HAS_COPIED_SECRET_FILES = true;
                }
                break;
            }
        }
    };
    final BashReader bashProcess = new BashReader("/", root, onExit);

    void algorithm() {
        loadMainMenu();
        /*
        * Sur un thread séparé, on compte l'heure et le jour
        */
        createTime(() -> {
            // Pour des raisons inconnues, time.interrupt() interrompt bien le thread,
            // mais on arrive pas à le remettre en route.
            // Donc on fait en sorte ici que `hour` et `day`
            // ne soient incrémentés que lorsqu'on le veut
            if (page == Page.GAME && TIME_PASSING && KIDNAPPING) { // KIDNAPPING = true signifie que le kidnapping est fini, c'est nécessaire pour s'assurer que le temps reste bloqué si on quitte le jeu
                hour++;
                if (hour == 24) {
                    hour = 0;
                    day++;
                }
                displayTime();
            }
            time.run();
        }, HOUR_DELAY*1000);
        /*
         * Nous ne voulons pas utiliser la méthode `readString` pour capturer les touches que le joueur saisit,
         * car il faudrait appuyer sur Entrer à chaque fois. Avec la tonne de déplacements que le joueur peut faire,
         * c'est trop handicapant.
         * On va donc écouter l'input de l'utilisateur en direct.
         * Ici, on initialise l'événement, et la fonction d'écoute est `void keyTypedInConsole(int key)`.
         */
        enableKeyTypedInConsole(true);
        while (!finishedTyping) {
            delay(500);
        }
        enableKeyTypedInConsole(false);
    }

    /**
     * Nous créons le temps.
     * Cette fonction crée un thread séparé pour incrémenter le temps de manière asynchrone.
     * @param runnable la fonction à exécuter au bout du délai
     * @param delay Le délai entre chaque exécution de la fonction `runnable`
     */
    void createTime(Runnable runnable, int delay) {
        time = new Thread(() -> {
            try {
                time.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                // ignore, for now at least
            }
        });
        time.start();
    }

    /**
     * Charge le menu principal.
     * Ceci est la page `Page.MENU`.
     */
    void loadMainMenu() {
        page = Page.MENU;
        selectedMenu = 1; // default value

        int width = 50;
        clearMyScreen();
        printEqualsRow(width);
        printEmptyLine();
        int n = printASCII(GAME_MAIN_TITLE_PATH);
        print(repeat(" ", 25) + RGBToANSI(new int[]{252, 191, 203}, false) + "ft. Julien Baste" + ANSI_RESET);
        printEmptyLines(2);
        printEqualsRow(width);
        printEmptyLine();

        if (!game_started) {
            saveCursorPosition(); // on sauvegarde avant le chargement pour pouvoir effacer le texte ensuite
            print(repeat(" ", 20) + "Chargement...");
            printEmptyLines(2);
            initializeColorsAndDialogs();
            initializeImportantCharacters();
            initializeAllMaps();
            initializeAllCommands();
            initializeAllTVInfo();
            initializeAllCredits();
            initializeHelp();
            initializeAllLessons();
            initializeAllPrisoners();
            initializeAllSecrets();
            delay(250); // au cas où le chargement est trop rapide, on veut que le joueur le voit

            restoreCursorPosition(); // on va écrire à la place du mot "Chargement..."
        }

        print(repeat(" ", 12));
        printBoldText("Microsoft World Conquest");
        printEmptyLines(2);

        // Le menu principal
        // ---

        // constante qui correspond au nombre de lignes affichées depuis le coin supérieur gauche de l'écran sur l'axe Y
        menuPosY = n + 9;
        println(repeat(" ", 20) + "> Jouer");
        println(repeat(" ", 20) + "  Commandes");
        println(repeat(" ", 20) + "  Crédits");
        println(repeat(" ", 20) + "  Succès");
        printEmptyLines(2);
        println("Appuie sur Entrer pour confirmer.");
        println("Appuie sur '" + getCommandOfUID(KEY_QUIT).getCurrentChar() + "' pour quitter.");
        printEmptyLine();
        printlnRightAlinedText("version " + VERSION, width);

        game_started = true;
    }

    /**
     * Sélectionne un choix dans le menu principal en fonction de la touche captée.
     * Le joueur peut appuyer sur la flèche du haut pour sélectionner le choix du haut,
     * ou appuyez sur la flèche du bas pour sélectionner le choix du bas.
     * Pour modéliser ça, on utilise un entier, "movement".
     * @param movement -1 pour un mouvement vers le haut, 1 pour un movement vers le bas
     */
    void selectMenuItem(int movement) {
        int futureChoice = selectedMenu + movement;
        if (futureChoice < 1 || futureChoice > MENU_ITEMS_NUMBER) {
            return; // do nothing.
        }
        saveCursorPosition();
        // 20 est une constante qui correspond au nombre d'espaces
        // depuis le côté gauche de l'écran dans le menu principal
        moveCursorTo(20, menuPosY);
        print("  "); // on supprime le ">" devant le choix actuellement sélectionné
        if (movement == 1) {
            print("\033[1D\033[1B" + ">"); // recule le curseur et le descend d'une ligne
        } else {
            print("\033[1D\033[1A" + ">"); // recule le curseur et le monte d'une ligne
        }
        restoreCursorPosition();
        selectedMenu = futureChoice;
        menuPosY += movement;
    }

    /**
     * Lorsque l'utilisateur appuie sur une touche,
     * cette fonction reçoit un code numérique correspondant à la touche entrée.
     * Il s'agit d'une fonction qui capture l'événement en direct.
     * @param a Le code numérique correspondant à la touche entrée par l'utilisateur lors de l'écoute de l'événement.
     */
    void keyTypedInConsole(int a) {
        if (page == Page.MENU) {
            switch (a) {
                case TOP_ARROW_KEY:
                    selectMenuItem(-1);
                    break;
                case BOTTOM_ARROW_KEY:
                    selectMenuItem(1);
                    break;
                case ENTER_KEY:
                    switch (selectedMenu) {
                        case 1:
                            playGame();
                            break;
                        case 2:
                            shortcutsPage();
                            break;
                        case 3:
                            creditsPage();
                            break;
                        case 4:
                            achievementsPage();
                            break;
                    }
                    break;
                default:
                    if (a == getKeyForCommandUID(KEY_QUIT)) {
                        finishedTyping = true; // ceci va fermer le programme.
                    }
            }
        } else if (page == Page.GAME) {
            if (WAITING_FOR_USER_INPUT) {
                // Le joueur n'est pas en train d'utiliser son clavier pour se mouvoir ou jouer,
                // non, on veut qu'il écrive quelque chose puis confirme en utilisant Entrer.
                // C'est le cas quand on veut qu'il entre des commandes BASH.
                
                if (a == ENTER_KEY) {
                    clearLine();
                    saveCursorPosition();
                    int h = getTotalHeight()+6;
                    for (int i = 0; i <= (CURRENT_NUM_OF_LINES_IN_OUTPUT < 13 ? 13 : CURRENT_NUM_OF_LINES_IN_OUTPUT)+2; i++) {
                        clearLine();
                        moveCursorTo(0,h+i);
                    }
                    restoreCursorPosition();
                    print("\r> ");
                    saveCursorPosition();
                    String trimmedInput = INPUT.trim();
                    if (trimmedInput.length() == 0) {
                        return;
                    }
                    BashResult result = bashProcess.executeCommand(bashProcess.parseCommand(trimmedInput));
                    String output = result.toString(true);
                    CURRENT_NUM_OF_LINES_IN_OUTPUT = output.length() - output.replaceAll("\\n", "").length();
                    println("\n\n" + output);
                    restoreCursorPosition();
                    INPUT = "";
                    return;
                } else if (a == TOP_ARROW_KEY) {
                    INPUT = bashProcess.historyHandler.onUp();
                } else if (a == BOTTOM_ARROW_KEY) {
                    INPUT = bashProcess.historyHandler.onDown();
                } if (a >= 32 && a < 127) { // Si l'utilisateur écrit quelque chose, on veut qu'il se limite aux caractères simples d'ASCII.
                    INPUT += (char)a;
                } else if (a == 127 && INPUT.length() >= 1) {
                    INPUT = INPUT.substring(0,INPUT.length()-1);
                }

                clearLine();
                // pour éviter le staircase effect, on s'assure de ne pas oublier le carriage return (\r),
                // ce qui nous permet de rester bien sur le côté gauche de l'écran quand on écrit.
                print("\r> " + INPUT);
                
                return;
            } else if (WAITING_CONFIRMATION_TO_END_THE_GAME) {
                if (a == CONFIRMATION_KEY) {
                    EVASION_STARTED = true;
                    TIME_PASSING = false; // pas besoin du temps normal
                    time = null;
                    removeCommandsPanel();
                    clearDialogAndMessage();
                    COLORS[GUARD_INDEX].ANSI = COLORS[GROUND_INDEX].ANSI; // supprime définitivement les gardes
                    COLORS[GUARD_INDEX].x = true; // supprime définitivement les gardes
                    COLORS[MATHIEU_INDEX].ANSI = COLORS[GROUND_INDEX].ANSI;
                    COLORS[MATHIEU_INDEX].x = true;
                    COLORS[BASTE_INDEX].ANSI = COLORS[GROUND_INDEX].ANSI;
                    COLORS[BASTE_INDEX].x = true;
                    COLORS[CELL_INDEX].ANSI = COLORS[GROUND_INDEX].ANSI;
                    COLORS[CELL_INDEX].x = true;
                    COLORS[PRISONER_INDEX_IN_CLASSROOM].ANSI = COLORS[GROUND_INDEX].ANSI;
                    COLORS[PRISONER_INDEX_IN_CLASSROOM].x = true;
                    COLORS[ACCESS_TO_COURTYARD].t = false;
                    COLORS[PRISON_DOOR_INDEX].ANSI = COLORS[GROUND_INDEX].ANSI;
                    COLORS[PRISON_DOOR_INDEX].x = true;
                    COLORS[PRISON_DOOR_INDEX].t = true;
                    COLORS[PRISON_DOOR_INDEX].movX = 0;
                    COLORS[PRISON_DOOR_INDEX].movY = -1;
                    printEvasionHelp();
                    printEmptyLine();
                    evasionThread = new Thread(() -> {
                        try {
                            evasionThread.sleep(1000);
                            evasionRemainingTime--;
                            if (evasionRemainingTime < 0) {
                                evasionThread = null;
                                // todo: game over
                                return;
                            }
                            saveCursorPosition();
                            // Entre le moment où on sauvegarde la position du curseur,
                            // et le moment où on écrit,
                            // il faut se dépêcher pour limiter les erreurs d'affichage
                            println("Il vous reste " + evasionRemainingTime + " secondes.   ");
                            restoreCursorPosition();
                            evasionThread.run();
                        } catch (Exception e) {
                            // ignore that
                        }
                    });
                    evasionThread.start();
                } else if (a == DENY_KEY) {
                    WAITING_CONFIRMATION_TO_END_THE_GAME = false;
                    turnOnPC();
                }
            }
            // On ne peut pas utiliser un `switch` ici
            // car les case doivent être des constantes.
            if (a == getKeyForCommandUID(KEY_QUIT)) {
                resetState();
                loadMainMenu();
                return;
            }
            // Meaning the player's trying to press keys when he's not allowed to move
            if (LOCK_KEYS) {
                return;
            }
            if (CURRENT_LESSON != null) {
                if (WAITING_FOR_ANSWER_TO_LESSON) {
                    switch (a) {
                        case ENTER_KEY:
                            print("\033[1A");
                            clearLine(); // on supprime la phrase explicative
                            if (selectedLessonAnswer == CURRENT_LESSON.goodAnswer) {
                                println("Bravo ! C'est la bonne réponse.");
                            } else {
                                println("Faux ! La bonne réponse était la réponse " + CURRENT_LESSON.goodAnswer + ".");
                            }
                            resetState();
                            LAST_LESSON_INDEX = day/7;
                            break;
                        case TOP_ARROW_KEY:
                            if (selectedLessonAnswer > 1) {
                                saveCursorPosition();
                                int height = getTotalHeight();
                                moveCursorTo(4,height+3+selectedLessonAnswer);
                                print(" ");
                                selectedLessonAnswer--;
                                moveCursorTo(4,height+3+selectedLessonAnswer); // 3 car emptyLine + question + emptyLine
                                print("*");
                                restoreCursorPosition();
                            }
                            break;
                        case BOTTOM_ARROW_KEY:
                            if (selectedLessonAnswer < 3) {
                                saveCursorPosition();
                                int height = getTotalHeight();
                                moveCursorTo(4,height+3+selectedLessonAnswer);
                                print(" ");
                                selectedLessonAnswer++;
                                moveCursorTo(4,height+3+selectedLessonAnswer); // 3 car emptyLine + question + emptyLine
                                print("*");
                                restoreCursorPosition();
                            }
                            break;
                    }
                } else if (a == getKeyForCommandUID(KEY_START_LESSON)) {
                    clearDialogAndMessage();
                    writeMessage(CURRENT_LESSON.question);
                    printEmptyLine();
                    println("  [*] " + CURRENT_LESSON.answers[0]);
                    println("  [ ] " + CURRENT_LESSON.answers[1]);
                    println("  [ ] " + CURRENT_LESSON.answers[2]);
                    printEmptyLine();
                    println("Appuie sur Entrer pour confirmer ton choix, et sur les flèches du clavier pour changer la sélection.");
                    WAITING_FOR_ANSWER_TO_LESSON = true;
                }
                return;
            }
            if (a == getKeyForCommandUID(KEY_WALK_TOP)) {
                movePlayerUp();
            } else if (a == getKeyForCommandUID(KEY_WALK_BOTTOM)) {
                movePlayerDown();
            } else if (a == getKeyForCommandUID(KEY_WALK_LEFT)) {
                movePlayerToLeft();
            } else if (a == getKeyForCommandUID(KEY_WALK_RIGHT)) {
                movePlayerToRight();
            } else if (a == getKeyForCommandUID(KEY_CONTACT)) {
                if (EVASION_STARTED) {
                    return;
                }
                if (TIME_PASSING) { // meaning we met Baste and we got kidnapped, so we're in the cell
                    if (currentHelp != null) {
                        currentHelpIndex++;
                        if (currentHelpIndex < length(currentHelp)) {
                            writeHelp(currentHelp[currentHelpIndex]);
                            printEmptyLine();
                            clearLine();
                            if (currentHelpIndex == length(currentHelp) - 1) {
                                println("Appuyez sur [" + getCommandOfUID(KEY_CONTACT).getCurrentChar() + "] pour terminer.");
                            } else {
                                println("Appuyez sur [" + getCommandOfUID(KEY_CONTACT).getCurrentChar() + "] pour continuer.");
                            }
                        } else if (currentHelpIndex == length(currentHelp)) {
                            clearDialogAndMessage();
                            currentHelp = null;
                            currentHelpIndex = 0;
                            COMMUNICATED_WITH_BASTE_FOR_THE_FIRST_TIME = true;
                        }
                    } else {
                        currentHelpIndex = 0;
                        if (KIDNAPPING && !DISCOVERED_CONTROL_PC && !HAS_LINUX) {
                            currentHelp = getHelpOfGroup(0);
                            currentHelpIndex = (COMMUNICATED_WITH_BASTE_FOR_THE_FIRST_TIME ? 3 : 0);
                        } else if (DISCOVERED_CONTROL_PC && !HAS_LINUX) {
                            currentHelp = getHelpOfGroup(1);
                        } else if (HAS_LINUX && !HAS_PASSWORD) {
                            currentHelp = getHelpOfGroup(2);
                        } else if (HAS_PASSWORD && !HAS_COPIED_SECRET_FILES) {
                            currentHelp = getHelpOfGroup(3);
                        } else {
                            currentHelp = getHelpOfGroup(4);
                        }
                        writeHelp(currentHelp[currentHelpIndex]);
                        if (length(currentHelp) > 1) {
                            printEmptyLine();
                            println("Appuyez sur [" + getCommandOfUID(KEY_CONTACT).getCurrentChar() + "] pour continuer.");
                        }
                    }
                }
            } else if (a == getKeyForCommandUID(KEY_INTERACT)) {
                if (EVASION_STARTED) {
                    return;
                }
                if (currentDialogs != null) {
                    clearDialogAndMessage();
                    currentGroupIndex++;
                    if (currentDialogs[currentGroupIndex] == null) {
                        if (MET_BASTE && !KIDNAPPING && currentDialogs[currentGroupIndex-1].colorIndex == BASTE_INDEX && currentMap.equals("devant-la-bibliotheque")) {
                            TIME_PASSING = false; // just in case the player left the game and came back between the beginning of the dialog and the kidnapping
                            KIDNAPPING = true;
                            LOCK_KEYS = true;
                            clearMyScreen();
                            new Thread(() -> {
                                try {
                                    println("Vous vous faites kidnapper...");
                                    Thread.sleep(2000);
                                    println("Tout le monde a été emmené avec vous,");
                                    Thread.sleep(2000);
                                    println("mais vous remarquez que Baste vous a filé une oreillette discrètement...");
                                    Thread.sleep(2000);
                                    println("Peut-être que vous pourrez communiquer, en secret !");
                                    Thread.sleep(4000);
                                    TIME_PASSING = true;
                                    teleportPlayerToNewMap("votre-cellule", 0, 0);
                                    LOCK_KEYS = false;
                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            }).start();
                            return;
                        }
                        resetDialogState();
                    } else {
                        writeDialog(currentDialogs[currentGroupIndex]);
                        printEmptyLine();
                        if (currentDialogs[currentGroupIndex+1] == null) {
                            println("[" + getCommandOfUID(KEY_INTERACT).getCurrentChar() + "] pour terminer le dialogue.");
                        } else {
                            println("[" + getCommandOfUID(KEY_INTERACT).getCurrentChar() + "] pour continuer le dialogue.");
                        }
                    }
                } else if (nearestInteractiveCell == TV_INDEX) {
                    for (int i = lastIndexOfTVInfo; i < length(TV_INFO); i++) {
                        if (TV_INFO[i].day == (day+1)) {
                            writeMessage("Vous écoutez la télé qui dit : " + TV_INFO[i].text);
                            lastIndexOfTVInfo = i;
                            break;
                        }
                    }
                    printEmptyLine();
                    println("Il reste " + (DEADLINE - day) + " jour" + (DEADLINE - day >= 2 ? 's' : "") + " avant la fin!");
                } else if (nearestInteractiveCell == PRISONER_INDEX) {
                    if (random() < 0.05) {
                        writeMessage("Détenu - \"C'est bientôt l'anniversaire du directeur, le 01 septembre.\"");
                        HAS_PASSWORD = true;
                    } else {
                        writeMessage("Détenu - " + PRISONERS_DIALOGS[(int)(random()*length(PRISONERS_DIALOGS))].replaceAll("\\$", ","));
                    }
                } else if (nearestInteractiveCell == BED_INDEX) {
                    if (WANTS_TO_SLEEP) {
                        // On revérifie l'heure ici car il peut se passer plusieurs heures 
                        // entre la première interaction et la confirmation
                        if (hour > 19 || hour < 9) {
                            clearMyScreen();
                            LOCK_KEYS = true;
                            TIME_PASSING = false;
                            new Thread(() -> {
                                try {
                                    println("Vous faites de beau rêve concernant du BASH...");
                                    hour = 9;
                                    day++;
                                    Thread.sleep(2000);
                                    clearMyScreen();
                                    displayMap();
                                    printPlayer(playerX,playerY);
                                    LOCK_KEYS = false;
                                    WANTS_TO_SLEEP = false;
                                    TIME_PASSING = true;
                                } catch (Exception e) {
                                    System.err.println(e);
                                }
                            }).start();
                        } else {
                            WANTS_TO_SLEEP = false;
                            writeMessage("Tu ne peux dormir que la nuit.");
                        }
                    } else {
                        if (hour > 19 || hour < 9) {
                            writeMessage("Il s'agit de votre lit. Vous voulez dormir ? Appuyez sur [" + getCommandOfUID(KEY_INTERACT).getCurrentChar() + "] pour dormir.");
                            WANTS_TO_SLEEP = true;
                        } else {
                            writeMessage("Vous ne pouvez pas dormir à cette heure-ci.");
                        }
                    }
                } else if (nearestInteractiveCell == PC_CONTROL_INDEX) {
                    if (!HAS_LINUX) {
                        writeMessage("Baste - " + (!DISCOVERED_CONTROL_PC ? "Tu as découvert le PC centrale de la prison ! " : "") + "Il te faut maintenant un moyen d'utiliser Linux. Via me voir.");
                    } else {
                        if (!HAS_USB_KEY) {
                            writeMessage("Baste - Tu as besoin d'une autre clé USB pour copier les données confidentielles.");
                        } else {
                            if (!HAS_PASSWORD) {
                                writeMessage("Baste - Il faut que tu trouves le mot de passe. Va te renseigner auprès des autres détenus. Je pense que c'est la date de naissance du directeur.");
                            } else {
                                if (HAS_COPIED_SECRET_FILES) {
                                    int numberOfSecretsFoundSoFar = 0;
                                    for (int i = 0; i < SECRETS.length; i++) {
                                        if (SECRETS[i].wasFound) {
                                            numberOfSecretsFoundSoFar++;
                                        }
                                    }
                                    if (numberOfSecretsFoundSoFar >= SECRETS_TRESHOLD) {
                                        writeMessage("Vous avez trouvé suffisamment de secrets pour démarrer l'évasion de la prison. Voulez-vous vous évader maintenant ?");
                                        println("Appuyez sur la touche [o] pour oui, [n] pour ignorer.");
                                        WAITING_CONFIRMATION_TO_END_THE_GAME = true;
                                        return;
                                    }
                                }
                                turnOnPC();
                            }
                        }
                    }
                    DISCOVERED_CONTROL_PC = true;
                } else if (nearestInteractiveCell == BASTE_INDEX && currentMap.equals("couloir-5")) {
                    if (DISCOVERED_CONTROL_PC && !HAS_LINUX) {
                        writeMessage("Baste - Hey, voilà la clé USB. Cependant, pour copier les données du PC, il va t'en falloir une autre.");
                        HAS_LINUX = true;
                    } else {
                        writeMessage("Baste - Coucou, je repense à du Bash aujourd'hui...");
                    }
                } else if (nearestInteractiveCell == MATHIEU_INDEX && HAS_LINUX && !HAS_USB_KEY && currentMap.equals("couloir-3")) {
                    writeMessage("Mathieu - Je t'ai vu parlé à Baste, et je suis prêt à t'aider. Tiens, la clé USB dont tu as besoin.");
                    root.appendFileElement(
                        new FileElement("USB-Mathieu", "/USB-Mathieu", new FileElement[]{
                            new FileElement("transp_cours21_handout-1.pdf", "/USB-Mathieu/transp_cours21_handout-1.pdf"),
                            new FileElement("transports-marchandises.mcd", "/USB-Mathieu/transports-marchandises.mcd"),
                        })
                    );
                    root.appendFileElement(
                        new FileElement("USB-Baste", "/USB-Baste", new FileElement[]{
                            new FileElement("42.txt", "/USB-Baste/42.txt", "Never gonna give you up!\n" + readASCII(RICK_PATH)),
                            new FileElement("ARCHLINUX.iso", "/USB-Baste/ARCHLINUX.iso"),
                            new FileElement("bible.txt", "/USB-Baste/bible.txt", """
                                Juliem Bastum senior sit amet formosus admodum, doctus homo, qui codicem noverat, complexus est.
                                In primo semestri nostro ei convenimus, cum adhuc iuvenes essemus et imperiti in primo amphitheatro Systematis introductorio.
                                Tam pridem, nonnulli ad hanc formidolosam nostram passionem futuram non suspicabantur, adeo ut alii dormirent, alii cum admiratione initiati sunt ad contemplandum charisma,
                                intelligentia et ingenium loquentis. Dum in spectaculis magnifici crines oculos nostros induxerant, eius oratio sollerter nos Linux introduxit cum honesto
                                studio nos eximendi ab influentia Microsoftus,quae conscientias cupider servaverat.
                                In memoria mea interdum obscuratur, quaedam perspicuitas erat, cum has auditores rarae pulchritudinis meminissem. Imbres et ventos profitebar ire loco sancto.
                                In capulus demersus sum ut oculos tantum haberem ei, et lectio diligenter parata. Tali passione descripsit operationes binariis terribiles, quae tamen depressionem plurium urit ex nobis.
                                Intantum fervorem, ut corpora et corda nostra etiam in unitatem foveantur.
                            """),
                            new FileElement("Thomas", "/USB-Baste/Thomas", new FileElement[]{
                               new FileElement("naval-battle.sh", "/USB-Baste/Thomas/naval-battle.sh", "#!/usr/bin/env bash\n\n...")
                            })
                        })
                    );
                    root.appendFileElement(new FileElement(".CODES-CELLULES", "/.CODES-CELLULES", getSecretCodesFileContent()));
                    HAS_USB_KEY = true;
                } else {
                    for (int i = 0; i < SECRETS.length; i++) {
                        if (SECRETS[i].colorIndex == nearestInteractiveCell) {
                            clearDialogAndMessage();
                            if (SECRETS[i].wasFound) {
                                writeMessage("Vous avez déjà trouvé ce secret.");
                            } else {
                                writeMessage(SECRETS[i].message);
                                printEmptyLine();
                                printBoldText("Secret trouvé !\n\r");
                                SECRETS[i].wasFound = true;
                                for (int y = 0; y < root.subElements.length; y++) {
                                    if (root.subElements[y].name == ".CODES-CELLULES") {
                                        root.subElements[y].fileContent = getSecretCodesFileContent();
                                        break;
                                    }
                                }
                                return;
                            }
                        }
                    }
                    boolean found_one_but_different_map = false;
                    for (int i = 0; i < length(DIALOGS); i++) {
                        if (DIALOGS[i].colorIndex == nearestInteractiveCell) {
                            // Certains dialogues ne peuvent se produire que dans une seule map précise.
                            // Cependant, certains dialogues peuvent débuter dans n'importe quelle map (donc valeur à `null`).
                            if (DIALOGS[i].map != null && !DIALOGS[i].map.equals(currentMap)) {
                                found_one_but_different_map = true;
                                continue;
                            }
                            // Puisqu'askip on peut pas utiliser ArrayList,
                            // on se fiche un peu des perf donc on donne une limite au pif
                            // et on se débrouille après
                            Dialog[] d = new Dialog[20]; // les messages du dialogue (chaque dialogue a un groupe unique, un entier, de messages)
                            int trueLength = 0;
                            boolean found = false;
                            for (int y = 0, e = 0; y < length(DIALOGS); y++) {
                                if (DIALOGS[y].group == DIALOGS[i].group) {
                                    // Pour la progression du jeu
                                    if (!MET_BASTE && DIALOGS[y].colorIndex == PC_INDEX && currentMap.equals("bibliotheque")) {
                                        MET_BASTE = true;
                                    }
                                    d[e++] = DIALOGS[y];
                                    found = true;
                                } else {
                                    // Si on a déjà trouvé le groupe,
                                    // sachant que tous les messages d'un même groupe se suivent,
                                    // alors dès qu'on est dans un autre groupe, inutile de continuer la boucle.
                                    if (found) {
                                        break;
                                    }
                                }
                            }
                            for (int j = 0; j < length(d); j++) {
                                if (d[j] == null) {
                                    trueLength = j;
                                    break;
                                }
                            }
                            if (DIALOGS[i].isRandom) {
                                writeDialog(d[(int)(random()*trueLength)]);
                                printEmptyLine();
                            } else {
                                writeDialog(d[0]);
                                printEmptyLine();
                                if (trueLength > 1) {
                                    currentDialogs = d;
                                    println("[" + getCommandOfUID(KEY_INTERACT).getCurrentChar() + "] pour continuer le dialogue en court.");
                                }
                            }
                            return;
                        }
                    }
                    if (found_one_but_different_map) {
                        writeMessage("Cette personne a l'air occupé, ne la dérangez pas...");
                    }
                }
            } else if (a == getKeyForCommandUID(KEY_START_LESSON)) {
                if (EVASION_STARTED || !currentMap.equals("salle-de-classe")) {
                    return;
                }

                clearDialogAndMessage();
                
                int index = day/7;
                if (index == LAST_LESSON_INDEX) {
                    writeMessage("Le cours est terminé.");
                    return;
                }

                String teacher;
                if (day % 7 == 1) { // Tuesday
                    CURRENT_LESSON = ENGLISH_LESSONS[index];
                    teacher = "Vanuxem - Prof d'anglais";
                } else if (day % 7 == 3) { // Thursday
                    CURRENT_LESSON = HISTORY_LESSONS[index];
                    teacher = "Cappelle - Prof d'histoire";
                } else if (day % 7 == 5) { // Saturday
                    CURRENT_LESSON = FRENCH_LESSONS[index];
                    teacher = "Carle - Prof de français";
                } else if (day % 7 == 6) { // Sunday
                    CURRENT_LESSON = MATHS_LESSONS[index];
                    teacher = "Delecroix - Prof de maths";
                } else {
                    writeMessage("Il n'y a pas cours aujourd'hui.");
                    return;
                }

                TIME_PASSING = false;
                writeMessage(teacher + " - " + CURRENT_LESSON.lesson);
                println("\nAppuie sur [" + getCommandOfUID(KEY_START_LESSON).getCurrentChar() + "] pour continuer");
            }
        } else if (page == Page.COMMANDS) {
            if (isWaitingForKeyInput) {
                // Si les touches ne changent pas d'ordre dans le fichier,
                // et il n'y a aucune raison que cela se produise,
                // alors selectedCommand - 1 correspond à l'indice de la commande sélectionnée dans COMMANDS
                int commandIndex = selectedCommand - 1;
                // On veut maintenant réassigner la touche et actualiser l'interface
                Command command = COMMANDS[commandIndex];
                command.key = a;
                saveCursorPosition();
                moveCursorTo(0, selectedCommandPosY);
                clearLine();
                println("[*] " + command.name + " (" + command.getOriginalChar() + ") : " + command.getCurrentChar());
                restoreCursorPosition();
                isWaitingForKeyInput = false;
            } else {
                switch (a) {
                    case TOP_ARROW_KEY:
                        isWaitingForKeyInput = false;
                        selectCommandItem(-1);
                        break;
                    case BOTTOM_ARROW_KEY:
                        isWaitingForKeyInput = false;
                        selectCommandItem(1);
                        break;
                    case ENTER_KEY:
                        isWaitingForKeyInput = !isWaitingForKeyInput;
                        break;
                    default:
                        if (a == getKeyForCommandUID(KEY_QUIT)) {
                            loadMainMenu();
                        }
                }
            }
        } else {
            if (a == getKeyForCommandUID(KEY_QUIT)) {
                loadMainMenu();
            }
        }
    }

    void loadEmptyPage() {
        clearMyScreen();
        println("Wooooow! Même si on a pas de vie, on a pas encore eu le temps de coder ça, sorry!");
        println("Appuie sur '" + getCommandOfUID(KEY_QUIT).getCurrentChar() + "' pour revenir au menu.");
    }

    void creditsPage() {
        page = Page.CREDITS;
        clearMyScreen();

        int width = 52;
        printEqualsRow(width);
        printEmptyLine();
        printASCII(CREDITS_TITLE_PATH);
        printEmptyLine();
        printEqualsRow(width);
        printEmptyLine();

        creditsThread = new Thread(() -> {
            try {
                creditsThread.sleep(200);
                if (page != Page.CREDITS) {
                    creditsThread.interrupt();
                    creditsThread = null;
                    nDisplayedCredits = 0;
                    currentCategory = "";
                    return;
                }
                // Techniquement, on devrait dans un premier temps réarranger la liste
                // de manière à trier les éléments en fonction de leur catégorie,
                // afin d'être sûr que pour une certaine catégorie tous les éléments se suivent.
                // Dans ce cas-ci, avec les restrictions imposées, on veut pas se mettre une balle :)
                // donc on fait juste gaffe à bien arranger le fichier directement sans vérification
                for (int i = nDisplayedCredits; i < nDisplayedCredits + 1 && i < length(CREDITS); i++) {
                    if (!currentCategory.equals(CREDITS[i].category)) {
                        currentCategory = CREDITS[i].category;
                        printEmptyLine();
                        printlnCenterAlignedText(makeBoldText(currentCategory), width);
                    }
                    printlnCenterAlignedText(CREDITS[i].name, width);
                }
                nDisplayedCredits++;
                if (nDisplayedCredits < length(CREDITS)) {
                    creditsThread.run();
                }
            }
            catch (Exception e){
                System.err.println(e);
            }
        });

        creditsThread.start();
    }

    void shortcutsPage() {
        page = Page.COMMANDS;
        selectedCommand = 1;

        clearMyScreen();
        printEqualsRow(81);
        printEmptyLine();
        int title_height = printASCII(COMMANDS_TITLE_PATH);
        printEmptyLine();
        printEqualsRow(81);
        printEmptyLine();
        println("Changer les commandes : ");
        printEmptyLine();
        println("Commande (valeur par défaut) : valeur actuelle");
        printEmptyLine();
        println("    > " + WALKING_COMMAND);
        selectedCommandPosY = title_height + 11;
        for (int i = 0; i < length(COMMANDS); i++) {
            if (COMMANDS[i].category == CommandCategory.WALK) {
                println("[" + (i == 0 ? '*' : ' ') + "] " + COMMANDS[i].name + " (" + COMMANDS[i].getOriginalChar() + ") : " + COMMANDS[i].getCurrentChar());
            }
        }
        printEmptyLine();
        println("    > " + INTERACTIVE_COMMAND);
        for (int i = 0; i < length(COMMANDS); i++) {
            if (COMMANDS[i].category == CommandCategory.INTERACT) {
                println("[ ] " + COMMANDS[i].name + " (" + COMMANDS[i].getOriginalChar() + ") : " + COMMANDS[i].getCurrentChar());
            }
        }
        printEmptyLine();
        println("    > " + SYSTEM_COMMAND);
        for (int i = 0; i < length(COMMANDS); i++) {
            if (COMMANDS[i].category == CommandCategory.SYSTEM) {
                println("[ ] " + COMMANDS[i].name + " (" + COMMANDS[i].getOriginalChar() + ") : " + COMMANDS[i].getCurrentChar());
            }
        }
        printEmptyLines(2);
        println("Sélectionner la commande à configurer avec les flèches directionnelles du clavier");
        println("et modifier la touche en appuyant d'abord sur Entrer, et ensuite sur la touche de remplacement désirée.");
        println("Pour quitter cette page, utilisez la touche configurée du sytème (par défaut 'q').");
    }

    void selectCommandItem(int movement) {
        int futureChoice = selectedCommand + movement;
        int numberOfCommands = length(COMMANDS);
        if (futureChoice < 1 || futureChoice > numberOfCommands) {
            return;
        }
        saveCursorPosition();
        moveCursorTo(2, selectedCommandPosY);
        print(" ");
        print("\033[1D");
        if (movement == 1) {
            if (selectedCommand == NUMBER_OF_WALKING_COMMANDS || selectedCommand == NUMBER_OF_WALKING_COMMANDS + NUMBER_OF_INTERACTION_COMMANDS) {
                print(repeat("\033[1B", 3));
                selectedCommandPosY += 3;
            } else {
                selectedCommandPosY += movement;
                print("\033[1B");
            }
            print("*");
        } else {
            if (selectedCommand == NUMBER_OF_WALKING_COMMANDS + 1 || selectedCommand == NUMBER_OF_WALKING_COMMANDS + NUMBER_OF_INTERACTION_COMMANDS + NUMBER_OF_SYSTEM_COMMANDS) {
                print(repeat("\033[1A", 3));
                selectedCommandPosY -= 3;
            } else {
                selectedCommandPosY += movement;
                print("\033[1A");
            }
            print('*');
        }
        restoreCursorPosition();
        selectedCommand = futureChoice;
    }

    void achievementsPage() {
        page = Page.ACHIEVEMENTS;
        loadEmptyPage();
        // todo.
    }

    /**
     * Initialise le jeu (Page.GAME).
     * Affiche la map actuelle, ainsi que le joueur.
     */
    void playGame() {
        page = Page.GAME;

        clearMyScreen();
        Map map = getMapOfName(currentMap);
        displayMap(map);
        printPlayer(playerX,playerY);

        displayTime();
    }

    /**
     * Affiche le temps dans l'interface graphique à une position précise.
     */
    void displayTime() {
        if (page != Page.GAME) {
            return;
        }
        int currentDay = (4 + day) % 7;
        Map map = getMapOfName(currentMap);
        int height = getGUIHeight(map);
        saveCursorPosition();
        moveCursorTo(0,height-1); // entre la map et la ligne de "=" du dessous
        clearLine();
        println(DAYS[currentDay] + " " + hour + "h, jour " + (day + 1));
        restoreCursorPosition();
    }

    /**
     * On veut positionner le joueur à une position précise (x;y).
     * Avec (0;0) le coin supérieur gauche.
     * Dans un premier temps, on doit vérifier s'il s'agit d'un mouvement autorisé,
     * S'il s'agit d'une téléportation, alors on la réalise directement ici.
     * 
     * Pour bouger le joueur, on sauvegarde la position actuelle du curseur,
     * On réécrit la couleur de la carte censée être à la position actuelle du joueur,
     * puis on bouge le curseur à la position souhaitée
     * afin d'écrire la couleur du pixel représentant le joueur.
     * Enfin, on remet le curseur à sa position initiale pour écouter les entrées suivantes.
     * @param x La position en X
     * @param y La position en Y
     */
    void displayPlayer(int x, int y) {
        /*
        * Vérifie si le mouvement souhaité par le joueur est possible.
        * Il y a deux raisons pour lesquelles l'utilisateur peut ne pas pouvoir bouger vers la position souhaitée :
        * - il y a un mur (hors de la map)
        * - il y a une case infranchissable (on va pas marcher sur un PC, une table ou de l'eau)
        * On notera qu'un mouvement sur une case permettant
        * le transport du joueur vers une autre map est considiéré comme autorisé,
        * et on réalise la téléportation immédiatement.
        */
        Map map = getMapOfName(currentMap);
        int[][] grid = map.grid;
        int currentColorIndex = grid[playerY][playerX];
        Color currentColor = COLORS[currentColorIndex];
        // S'il s'agit d'une passerelle vers une autre carte,
        // on vérifie si le mouvement appliqué permet d'y accéder.
        if (currentColor.t) {
            int shiftX = x - playerX; // le déplacement qui a été réalisé sur l'axe X
            int shiftY = y - playerY; // le déplacement qui a été réalisé sur l'axe Y
            if (shiftX == currentColor.movX && shiftY == currentColor.movY) {
                // Pour l'évasion de la prison, on ajoute la téléportation dynamiquement
                // On vérifie ici s'il s'agit bien du bon mouvement pour sortir et c'est ainsi que le jeu s'arrête.
                if (currentColorIndex == PRISON_DOOR_INDEX) {
                    // todo: end of the game
                    clearMyScreen();

                    return;
                }
                // Si le joueur est dans la bibliothèque et n'a pas rencontré Baste,
                // alors il ne doit pas en sortir. C'est propre à la progression du jeu.
                // Puisqu'il spawn dans la bibliothèque et qu'il n'y a qu'une seule sortie,
                // suffit de vérifier s'il a parlé à Baste.
                if (!MET_BASTE) {
                    return;
                } else if (!COMMUNICATED_WITH_BASTE_FOR_THE_FIRST_TIME && currentMap.equals("votre-cellule")) {
                    return;
                }
                teleportPlayerToNewMap(currentColor.toMap, currentColor.toX, currentColor.toY);
                return;
            }
        }
        
        // Il s'agit d'un déplacement normal quelque part sur la carte,
        // il nous faut vérifier si ce mouvement ne fait pas sortir le joueur en-dehors de celle-ci.
        int maxX = length(grid[0]);
        int maxY = length(grid);
        if (x >= maxX || y >= maxY || x < 0 || y < 0) {
            return;
        }

        // Il peut y avoir des murs, qui correspondent à des pixels transparents limitrophes.
        int colorIndexOfTarget = grid[y][x];
        if (colorIndexOfTarget == -1) {
            return;
        }

        // Enfin, le pixel ciblé peut être considéré comme infranchissable.
        Color colorOfTarget = COLORS[colorIndexOfTarget];
        if (!colorOfTarget.x) {
            return;
        }

        // ---
        // Il s'agit d'un déplacement autorisé sur la carte actuelle

        saveCursorPosition();

        // Première étape : réécrire la bonne couleur à la position actuelle du joueur.
        int previousPos = map.grid[playerY][playerX];
        Color color = previousPos == -1 ? null : COLORS[previousPos];
        int xShift = getXShift();
        int yShift = getYShift();
        moveCursorTo(playerX*PIXEL_SIZE + xShift, playerY + yShift);
        if (previousPos == -1) {
            printTransparentPixel();
        } else {
            printPixel(color);
        }

        // Deuxième étape : écrire le pixel du joueur
        moveCursorTo(x*PIXEL_SIZE + xShift, y + yShift);
        printPlayer();

        restoreCursorPosition();
        // On définit les coordonnées données
        // comme étant la nouvelle position du joueur.
        playerX = x;
        playerY = y;

        if (EVASION_STARTED) {
            return;
        }

        saveCursorPosition();
        int previousValue = nearestInteractiveCell;
        if ((nearestInteractiveCell = hasInteractiveCellNearPlayer()) != previousValue) {
            displayCommandsPanel();
        }
        restoreCursorPosition();

        clearDialogAndMessage();
        resetDialogState();
        WANTS_TO_SLEEP = false;
    }

    /**
     * Vérifie si dans un carré autour du joueur il existe une case interactive.
     * S'il y en a une, on renvoie l'indice de la couleur du pixel, afin de l'identifier
     * et savoir quoi faire lorsque le joueur appuie sur la touche d'interactivité (par défaut, 'f').
     * Une case interactive peut être :
     * - une couleur possédant un dialogue
     * - la télé
     * @return La couleur de la cellule interactive la plus proche s'il y a une case interactive proche du joueur, `-1` sinon.
     */
    int hasInteractiveCellNearPlayer() {
        int[][] grid = getMapOfName(currentMap).grid;
        int height = length(grid);
        int width = length(grid[0]);
        int cell = -1;
        if (playerY-1 >= 0 && isCellInteractive(cell = grid[playerY-1][playerX])) return cell; // top
        if (playerX+1 < width && isCellInteractive(cell = grid[playerY][playerX+1])) return cell; // right
        if (playerY+1 < height && isCellInteractive(cell = grid[playerY+1][playerX])) return cell; // bottom
        if (playerX-1 >= 0 && isCellInteractive(cell = grid[playerY][playerX-1])) return cell; // left
        return -1;
    }

    /**
     * Retourne `true` si la cellule donnée est interactive.
     * @param cell Le numéro de la couleur de cette cellule.
     * @return Un booléan indiquant si cette couleur est interactive ou non.
     */
    boolean isCellInteractive(int cell) {
        return cell != -1 && (COLORS[cell].i || cell == TV_INDEX || cell == PRISONER_INDEX || cell == PC_CONTROL_INDEX);
    }

    /**
     * Retourne la largeur entre le côté gauche (x=0) et le début de la map actuellement affichée.
     * Le but est de positionner le joueur correctement dans la map malgré le GUI.
     * Note: ceci tient compte de `PIXEL_SIZE`.
     * @return Le décalage en X.
     */
    int getXShift() {
        return GUI_HORIZONTAL_MARGIN * PIXEL_SIZE - 1;
    }

    /**
     * Retourne la hauteur entre le haut (y=0) et le début de la map actuellement affichée.
     * Le but est de positionner le joueur correctement dans la map malgré le GUI.
     * @return Le décalage en Y.
     */
    int getYShift() {
        return GUI_VERTICAL_MARGIN + 3; // +3 pour lignes vides, equals row et le nom de la map
    }

    /**
     * Retourne la hauteur totale de l'interface graphique
     * @param map La map actuelle dont nous avons besoin de connaître la hauteur.
     * @return Un entier correspondant à la hauteur totale de l'interface graphique.
     */
    int getGUIHeight(Map map) {
        return (GUI_VERTICAL_MARGIN * 2) + length(map.grid) + 4; // +2 pour les lignes de "=" et +1 car on y affiche aussi le temps +1 pour le nom de la map
    }

    /**
     * Retourne la largeur totale de l'interface graphique.
     * @param map La map actuelle dont nous avons besoin de connaître la largeur.
     * @return Un entier correspondant à la largeur totale de l'interface graphique.
     */
    int getGUIWidth(Map map) {
        return length(map.grid[0])*PIXEL_SIZE+(GUI_HORIZONTAL_MARGIN*2);
    }

    /**
     * Retourne la hauteur du GUI + la hauteur du panneau de commandes.
     */
    int getTotalHeight() {
        return getGUIHeight(getMapOfName(currentMap)) + NUMBER_OF_INTERACTION_COMMANDS;
    }

    /**
     * Place le curseur à une certaine position sur la carte.
     * @param x La coordonnée en X
     * @param y La coordonnée en Y
     */
    void moveCursorTo(int x, int y) {
        print("\033[" + y + ";" + x + "H");
    }

    /**
     * Supprime tout ce qu'il y avait dans le terminal avant l'entrée de la commande d'exécution du programme.
     * On utilise une fonction différente de celle de iJava (`clearScreen`) car la nôtre force le curseur
     * à se positionner au (0;0) tel que `moveCursorTo` le dicte.
     * Étonnamment, nous avons observer un comportement différent entre notre fonction et `clearScreen`.
     * 
     * Il est très important de repositionner correctement l'affichage dans le coin supérieur gauche.
     * Sans ça, il était possible que le curseur ne soit pas sur la bonne ligne, causant des comportements inattendus et étranges.
     */
    void clearMyScreen() {
        print("\033[2J");
        moveCursorTo(0,0);
    }

    /**
     * On veut effacer le message de la télé
     * donc on place le curseur après la map et le panneau de commandes,
     * et on clear les 5 lignes suivantes.
     */
    void clearDialogAndMessage() {
        int totalHeight = getTotalHeight() + 1;
        for (int i = 0; i < 10; i++) {
            moveCursorTo(0,totalHeight+i);
            clearLine();
        }
        moveCursorTo(0,totalHeight);
        saveCursorPosition();
    }

    /**
     * Réinitialise toutes les variables globales propre à un état spécifique, sachant que l'on peut quitter le jeu à tout moment.
     */
    void resetState() {
        resetDialogState();
        CURRENT_LESSON = null;
        TIME_PASSING = true;
        WAITING_FOR_ANSWER_TO_LESSON = false;
        selectedLessonAnswer = 1;
        selectedCommandPosY = 0;
        currentHelp = null;
        currentHelpIndex = 0;
    }

    /**
     * Si le joueur quitte le dialogue, on veut que ce soit reset, aussi quand c'est fini.
     */
    void resetDialogState() {
        currentDialogs = null;
        currentGroupIndex = 0;
    }

    /**
     * Enregistre la position actuelle du curseur.
     * Le curseur sera dirigé vers cette position lors d'un appelle à la fonction `restoreCursorPosition`.
     */
    void saveCursorPosition() {
        print("\033[s");
    }

    /**
     * On revient à la dernière position sauvegardée du curseur.
     */
    void restoreCursorPosition() {
        print("\033[u");
    }

    void movePlayerUp() {
        displayPlayer(playerX, playerY-1);
    }

    void movePlayerDown() {
        displayPlayer(playerX, playerY+1);
    }

    void movePlayerToLeft() {
        displayPlayer(playerX-1, playerY);
    }

    void movePlayerToRight() {
        displayPlayer(playerX+1, playerY);
    }

    /**
     * Le joueur s'est déplacé sur une case dite de "passerelle" de telle façon qu'il doit être amené sur une autre carte.
     * C'est comme ça que l'on gère la "téléportation" entre deux cartes.
     * En bref, c'est le passage d'une carte à une autre quand on franchit une porte, par exemple.
     * @param map La carte vers laquelle rediriger le joueur.
     * @param targetX La position en X du joueur dans la nouvelle carte.
     * @param targetY La position en Y du joueur dans la nouvelle carte.
     */
    void teleportPlayerToNewMap(String map, int targetX, int targetY) {
        clearMyScreen();
        currentMap = map;
        displayMap();
        playerX = targetX;
        playerY = targetY;
        printPlayer(playerX,playerY);
        displayTime();
    }

    /**
     * Converti une couleur RGB au format ANSI.
     * La standardisation ANSI nous permet de colorer du texte, ou de le surligner.
     * @param rgb Une liste de 3 nombres entre 0 et 255 inclus, selon le format RGB.
     * @param backgroundColor Vrai si l'on veut que la couleur soit sur le fond du texte. Le texte aurait donc la couleur par défaut.
     * @return La couleur donnée au format ANSI, colorant le fond ou le texte.
     */
    String RGBToANSI(int[] rgb, boolean backgroundColor) {
        return "\u001b[" + (backgroundColor ? "48" : "38") + ";2;" + rgb[0] + ";" + rgb[1] + ";" + rgb[2] + "m";
    }

    /**
     * Charge l'interface réservée pour la console du PC de la salle de contrôle.
     * Ceci permet au joueur d'écrire des commandes BASH afin d'avancer dans le jeu. 
     */
    void turnOnPC() {
        writeMessage("Il s'agit du PC qui contrôle toute la prison" + (DISCOVERED_CONTROL_PC && HAS_PASSWORD ? " et tu as déjà trouvé le mot de passe" : "") + " ! Tu peux y entrer des commandes.");
        println("À tout moment, entre la commande \"exit\" et appuie sur Entrer pour quitter. Entre \"help\" pour obtenir de l'aide.\n");
        print("> ");
        TIME_PASSING = false;
        WAITING_FOR_USER_INPUT = true;
    }

    /**
     * Lis le fichier contenant toutes les couleurs et autre métadonnées associées à ces couleurs.
     * Chaque couleur a des metadonnées associées à elle qui définissent des propriétés uniques.
     * Elles peuvent avoir les propriétés suivantes :
     * - être franchissable ou non (est-ce qu'on peut marcher dessus ou non)
     * - être interactive (un PNJ par exemple, ce qui n'est pas encore implémenté)
     * - être un chemin vers une autre map (exemple: une porte menant sur une autre salle), ce qu'on appelle la "téléportation"
     * Pour avoir toutes les données nécessaires, on lit plusieurs fichiers :
     * - La charte de couleurs (`../assets/0-colors.csv`)
     * - Les téléportations possibles (`../assets/0-teleportations.csv`)
     * - Les dialogyes (`../assets/0-dialogs.csv`)
     * Cette fonction ne sera appelée qu'une seule fois lors de l'initialisation du jeu.
     */
    void initializeColorsAndDialogs() {
        CSVFile colors = loadCSV(COLORS_PATH);
        CSVFile teleportations = loadCSV(TELEPORTATIONS_PATH);
        CSVFile dialogs = loadCSV(DIALOGS_PATH);
        int nColors = rowCount(colors); // nombre de couleurs
        int nTeleportations = rowCount(teleportations); // nombre de pixels menant à une téléportation
        int nDialogs = rowCount(dialogs);
        int x,i,r,g,b,color; // toutes les données sous forme d'entier qu'on peut extraire des fichiers

        // Nous sommes obligés de redefinir COLORS
        // pour que nous ayons une liste de taille prédéfinie
        // car askip on peut pas utiliser ArrayList!
        COLORS = new Color[nColors-1];
        DIALOGS = new Dialog[nDialogs-1];

        for (int y=1;y<nColors;y++) {
            x = stringToInt(getCell(colors, y, 1));
            r = stringToInt(getCell(colors, y, 2));
            g = stringToInt(getCell(colors, y, 3));
            b = stringToInt(getCell(colors, y, 4));
            COLORS[stringToInt(getCell(colors, y, 0))] = newColor(r,g,b,x==1);
        }

        for (int y=1;y<nTeleportations;y++) {
            color = stringToInt(getCell(teleportations, y, 0));
            COLORS[color].t = true;
            COLORS[color].toMap = getCell(teleportations, y, 1);
            COLORS[color].movX = stringToInt(getCell(teleportations, y, 2));
            COLORS[color].movY = stringToInt(getCell(teleportations, y, 3));
            COLORS[color].toX = stringToInt(getCell(teleportations, y, 4));
            COLORS[color].toY = stringToInt(getCell(teleportations, y, 5));
        }

        for (int y=1;y<nDialogs;y++) {
            color = stringToInt(getCell(dialogs, y, 1));
            COLORS[color].i = true; // for easier detections near the player
            Dialog dialog = new Dialog();
            dialog.group = stringToInt(getCell(dialogs, y, 0));
            dialog.colorIndex = color;
            dialog.setMap(getCell(dialogs, y, 2).replaceAll("^\"(.*)\"$", "$1")); // removing the quotes
            dialog.narratorName = getCell(dialogs, y, 3);
            dialog.text = getCell(dialogs, y, 4).replaceAll("\\$", ","); // replacing the "$" by "," because iJava doesn't read CSV correctly
            dialog.isRandom = stringToInt(getCell(dialogs, y, 5)) == 1;
            DIALOGS[y-1] = dialog;
        }
    }

    /**
     * Crée une instance de Color avec les metadonnées de base.
     * Les autres métadonnées sont ajoutés ensuite (elles ont des valeurs par défaut).
     * @param r La composante rouge
     * @param g La composante verte
     * @param b La composante bleue
     * @param x Si c'est franchissable ou non
     */
    Color newColor(int r, int g, int b, boolean x) {
        Color color = new Color();
        color.ANSI = RGBToANSI(new int[]{r,g,b}, true);
        color.x = x;
        return color;
    }

    /**
     * Initialise les variables importantes du jeu.
     * En effet, pour progresser dans le jeu il va falloir interagir avec certaines personnes.
     * Il faut donc savoir le numéro unique attribué à leur couleur.
     * Ce fichier liste ces informations.
     */
    void initializeImportantCharacters() {
        CSVFile file = loadCSV(IMPORTANT_CHARACTERS_PATH);
        String name;
        int colorIndex;
        
        for (int y=1;y<rowCount(file);y++) {
            name = getCell(file, y, 1);
            colorIndex = stringToInt(getCell(file, y, 0));
            // Ces noms ne changeront jamais.
            switch (name) {
                case "PC": PC_INDEX = colorIndex; break;
                case "TV": TV_INDEX = colorIndex; break;
                case "Baste": BASTE_INDEX = colorIndex; break;
                case "Bed": BED_INDEX = colorIndex; break;
                case "PC_CONTROL": PC_CONTROL_INDEX = colorIndex; break;
                case "Mathieu": MATHIEU_INDEX = colorIndex; break;
                case "Prisoner": PRISONER_INDEX = colorIndex; break;
                case "Guard": GUARD_INDEX = colorIndex; break;
                case "Ground": GROUND_INDEX = colorIndex; break;
                case "Cell": CELL_INDEX = colorIndex; break;
                case "Access_to_courtyard": ACCESS_TO_COURTYARD = colorIndex; break;
                case "Prisoner_classroom": PRISONER_INDEX_IN_CLASSROOM = colorIndex; break;
                case "Prison_door": PRISON_DOOR_INDEX = colorIndex; break;
            }
        }
    }

    void initializeHelp() {
        CSVFile file = loadCSV(HELP_PATH);
        int count = rowCount(file);

        HELP = new Help[count-1];

        for (int y=1;y<count;y++) {
            Help help = new Help();
            help.group = stringToInt(getCell(file, y, 0));
            help.text = getCell(file, y, 1).replaceAll("\\$", ",");
            HELP[y-1] = help;
        }
    }

    /**
     * Pour gagner en fluidité lors du jeu, nous allons charger toutes les cartes du jeu lors de l'initialisation du jeu.
     * Toutes les cartes sont stockées dans "../assets/maps/".
     * Chaque carte est une matrice où chaque nombre est
     * le numéro unique associé à une couleur dans la charte (`0-colors.csv`)
     */
    void initializeAllMaps() {
        String[] mapsFiles = getAllFilesFromDirectory(MAPS_PATH);
        int numberOfMaps = length(mapsFiles);

        // Encore une fois, on n'est pas "autorisés" à utiliser ArrayList ;(
        // c'est trop "avancé" :(
        // Dcp, on redéfinit la variable globale ici pour avoir la bonne longueur
        MAPS = new Map[numberOfMaps];

        for (int i = 0; i < numberOfMaps; i++) {
            String filename = mapsFiles[i];
            CSVFile file = loadCSV(MAPS_PATH + filename);
            int[][] grid = createMapGridFromCSVContent(file);
            MAPS[i] = newMap(filename, grid);
        }
    }

    /**
     * On crée une carte du jeu en fonction des données lues du fichier CSV.
     * @param name Le nom de la map, chaque map aura un nom unique.
     * @param grid La matrice de la carte.
     * @return Une instance de Map
     */
    Map newMap(String name, int[][] grid) {
        Map map = new Map();
        map.name = name.substring(0,name.length()-4); // on veut retirer le ".csv", donc -4
        map.grid = grid;
        return map;
    }

    /**
     * Initialise toutes les commandes stockées dans le fichier `../assets/0-commands.csv`.
     */
    void initializeAllCommands() {
        CSVFile file = loadCSV(COMMANDS_PATH);
        int nCommands = rowCount(file);
        
        COMMANDS = new Command[nCommands-1];

        for (int i=1;i<nCommands;i++) {
            Command command = new Command();
            command.name = getCell(file, i, 0);
            command.category = getCategoryBasedOnName(getCell(file, i, 1));
            command.key = stringToInt(getCell(file, i, 2));
            command.originalKey = stringToInt(getCell(file, i, 2));
            command.uid = getCell(file, i, 3);
            COMMANDS[i-1] = command;
        }
    }

    /**
     * Retourne une instance de la commande demandée.
     * @param uid L'identifiant unique attachée à cette commande.
     * @return L'instance de Command.
     */
    Command getCommandOfUID(String uid) {
        for (int i = 0; i < length(COMMANDS); i++) {
            if (COMMANDS[i].uid.equals(uid)) {
                return COMMANDS[i];
            }
        }
        return null;
    }

    /**
     * Retourne le numéro de la touche attachée à la commande désirée.
     * @param uid L'identifiant unique attachée à cette commande.
     * @return L'entier qui correspond au code numérique de la touche.
     */
    int getKeyForCommandUID(String uid) {
        Command c = getCommandOfUID(uid);
        return c == null ? -1 : c.key;
    }

    /**
     * Retourne un enum de la catégorie de la commande basée sur le nom de la catégorie telle écrite dans le CSV.
     * @param name Le nom de la catégorie d'une commande.
     * @return La valeur de l'enum correspondant.
     */
    CommandCategory getCategoryBasedOnName(String name) {
        switch (name) {
            case WALKING_COMMAND: return CommandCategory.WALK;
            case INTERACTIVE_COMMAND: return CommandCategory.INTERACT;
            case SYSTEM_COMMAND: return CommandCategory.SYSTEM;
        }
        return null;
    }

    /**
     * Initialise toutes les infos de la télé (dans la cellule du joueur)
     */
    void initializeAllTVInfo() {
        CSVFile file = loadCSV(TVINFO_PATH);
        int nInfo = rowCount(file);

        TV_INFO = new TVInfo[nInfo-1];

        for (int i=1;i<nInfo;i++) {
            TVInfo info = new TVInfo();
            info.day = stringToInt(getCell(file, i, 0));
            info.text = getCell(file, i, 1);
            TV_INFO[i-1] = info;
        }
    }

    /**
     * Initialise tous les crédits.
     * Le but est de pouvoir arranger l'affichage ordonné par catégorie.
     */
    void initializeAllCredits() {
        CSVFile file = loadCSV(CREDITS_PATH);
        int nCredits = rowCount(file);

        CREDITS = new Credit[nCredits-1];
        
        for (int y=1;y<nCredits;y++) {
            Credit credit = new Credit();
            credit.name = getCell(file, y, 0);
            credit.category = getCell(file, y, 1);
            CREDITS[y-1] = credit;
        }
    }

    /**
     * Initialise tous les cours dispensés dans la salle de classe à intervalles réguliers.
     */
    void initializeAllLessons() {
        CSVFile english_file = loadCSV(ENGLISH_LESSONS_PATH);
        CSVFile french_file = loadCSV(FRENCH_LESSONS_PATH);
        CSVFile maths_file = loadCSV(MATHS_LESSONS_PATH);
        CSVFile history_file = loadCSV(HISTORY_LESSONS_PATH);
        int nEnglish = rowCount(english_file);
        int nFrench = rowCount(french_file);
        int nMaths = rowCount(maths_file);
        int nHistory = rowCount(history_file);

        ENGLISH_LESSONS = new Lesson[nEnglish];
        FRENCH_LESSONS = new Lesson[nFrench];
        MATHS_LESSONS = new Lesson[nMaths];
        HISTORY_LESSONS = new Lesson[nHistory];

        for (int y=1;y<nEnglish;y++) ENGLISH_LESSONS[y-1] = createLesson(getCell(english_file, y, 0), getCell(english_file, y, 1), new String[]{getCell(english_file, y, 2), getCell(english_file, y, 3), getCell(english_file, y, 4)}, stringToInt(getCell(english_file, y, 5)));
        for (int y=1;y<nFrench;y++) FRENCH_LESSONS[y-1] = createLesson(getCell(french_file, y, 0), getCell(french_file, y, 1), new String[]{getCell(french_file, y, 2), getCell(french_file, y, 3), getCell(french_file, y, 4)}, stringToInt(getCell(french_file, y, 5)));
        for (int y=1;y<nMaths;y++) MATHS_LESSONS[y-1] = createLesson(getCell(maths_file, y, 0), getCell(maths_file, y, 1), new String[]{getCell(maths_file, y, 2), getCell(maths_file, y, 3), getCell(maths_file, y, 4)}, stringToInt(getCell(maths_file, y, 5)));
        for (int y=1;y<nHistory;y++) HISTORY_LESSONS[y-1] = createLesson(getCell(history_file, y, 0), getCell(history_file, y, 1), new String[]{getCell(history_file, y, 2), getCell(history_file, y, 3), getCell(history_file, y, 4)}, stringToInt(getCell(history_file, y, 5)));
    }

    /**
     * Fake constructor of the Lesson OBJECT
     */
    Lesson createLesson(String lesson, String question, String[] answers, int goodAnswer) {
        Lesson l = new Lesson();
        l.lesson = length(lesson.replaceAll("\"", "")) == 0 ? "Interrogation surprise !" : lesson.replaceAll("\\$", ",");
        l.question = question.replaceAll("\\$", ",");
        l.answers = answers;
        l.goodAnswer = goodAnswer;
        return l;
    }

    /**
     * Initialise tous les dialogues aléatoires des prisoniers.
     */
    void initializeAllPrisoners() {
        CSVFile file = loadCSV(PRISONERS_PATH);
        int n = rowCount(file);
        PRISONERS_DIALOGS = new String[n-1];
        for (int y=1;y<n;y++) PRISONERS_DIALOGS[y-1] = getCell(file, y, 0);
    }

    /**
     * Initialise tous les secrets du jeu pour l'évasion.
     */
    void initializeAllSecrets() {
        CSVFile file = loadCSV(SECRETS_PATH);
        int n = rowCount(file);
        SECRETS = new Secret[n-1];
        for (int y=1;y<n;y++) {
            Secret secret = new Secret();
            secret.colorIndex = stringToInt(getCell(file, y, 0));
            secret.message = getCell(file, y, 1);
            secret.name = getCell(file, y, 2);
            secret.code = getCell(file, y, 3);
            COLORS[secret.colorIndex].i = true;
            SECRETS[y-1] = secret;
        }
    }

    /**
     * Gets the content of the file named ".CODES-CELLULES".
     */
    String getSecretCodesFileContent() {
        String content = "Entrez au moins " + SECRETS_TRESHOLD + " codes pour déverrouiller toutes les cellules.\n";
        for (int i = 0; i < SECRETS.length; i++) {
            content += "    Code " + (i+1) + " : " + (SECRETS[i].wasFound ? SECRETS[i].code : "") + "\n";
        }
        return content;
    }

    /**
     * Lis un fichier CSV pour le convertir en une matrice.
     * @param file Le fichier CSV chargé.
     * @return Une grille où chaque élément est le pixel d'une carte.
     */
    int[][] createMapGridFromCSVContent(CSVFile file) {
        int nbLignes = rowCount(file);
        int nbCol = columnCount(file);
        int[][] grid = new int[nbLignes-1][nbCol];
        for (int lig=1;lig<nbLignes;lig++) { // on commence à 1 pour éviter la première ligne du fichier csv
            for (int col=0;col<nbCol;col++) {
                String cell = getCell(file,lig,col);
                int index = stringToInt(cell);
                grid[lig-1][col] = index;
            }
        }
        return grid;
    }

    /**
     * Retourne l'instance de Map en fonction du nom de la carte actuelle.
     * @param name Le nom de la carte.
     * @return Une instance de Map stockée dans `MAPS`.
     */
    Map getMapOfName(String name) {
        for (int i = 0; i < length(MAPS); i++) {
            if (MAPS[i].name.equals(name)) {
                return MAPS[i];
            }
        }
        return null; // should never happen, except if we made a typo
    }

    /**
     * Affiche la carte actuelle dans le terminal.
     * On notera qu'un pixel = 2 caractères (deux espaces blancs) car ensemble ils forment un carré.
     * Un caractère, par défaut, forme un rectangle.
     */
    void displayMap() {
        Map map = getMapOfName(currentMap);
        displayMap(map);
    }

    /**
     * Ici, la fonction qui permet d'afficher n'importe quelle map au milieu de son interface.
     * Ceci comprend donc les lignes de "=" et le nom de la map, en plus des espaces vides pour aérer l'interface.
     * @param Map La carte à afficher
     */
    void displayMap(Map map) {
        int[][] grid = map.grid;
        int mapHeight = length(grid);
        int mapWidth = length(grid[0]);
        int equalsRowLength = getGUIWidth(map);
        printEqualsRow(equalsRowLength);
        printEmptyLines(GUI_VERTICAL_MARGIN);
        println("(" + formatMapName(map.name) + ")");
        for (int lig=0;lig<mapHeight;lig++) {
            print(repeat(" ", GUI_HORIZONTAL_MARGIN));
            for (int col=0;col<mapWidth;col++) {
                int n = grid[lig][col];
                if (n == -1) {
                    printTransparentPixel();
                } else {
                    printPixel(COLORS[n]);
                }
            }
            println(""); // très important
        }
        printEmptyLines(GUI_VERTICAL_MARGIN + 1); // +1 pour la ligne réservée à l'heure
        printEqualsRow(equalsRowLength);
        if (!EVASION_STARTED) {
            displayCommandsPanel();
        } else {
            printEvasionHelp();
        }
    }

    /**
     * Formatte le nom d'une carte pour que ce soit plus joli à lire.
     * Par exemple : "salle-de-controle" deviendra "Salle de controle"
     * @param name Le nom de la map (nom du fichier sans le '.csv' à la fin)
     * @return Le nom de la carte que l'on peut afficher à l'utilisateur.
     */
    String formatMapName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).replaceAll("-", " ");
    }

    /**
     * Affiche le panneau avec toutes les touches que l'utilisateur peut utiliser.
     * Ceci est vérifié à chaque déplacement.
     * Si l'on détecte une nouvelle touche,
     * alors on recrée l'intégralité du panneau en appelant cette fonction.
     */
    void displayCommandsPanel() {
        removeCommandsPanel();
        for (int i = 0; i < length(COMMANDS); i++) {
            if (COMMANDS[i].category == CommandCategory.INTERACT) {
                if (COMMANDS[i].uid.equals(KEY_INTERACT) && nearestInteractiveCell == -1) {
                    continue;
                }
                if (COMMANDS[i].uid.equals(KEY_START_LESSON) && (CURRENT_LESSON != null || !currentMap.equals("salle-de-classe"))) {
                    continue;
                }
                if (COMMANDS[i].uid.equals(KEY_CONTACT) && !TIME_PASSING) {
                    continue;
                }
                println("   [" + COMMANDS[i].getCurrentChar() + "] " + COMMANDS[i].name);
            }
        }
    }

    /**
     * Efface le panneau affichant les touches d'interaction que l'on peut utiliser.
     */
    void removeCommandsPanel() {
        Map map = getMapOfName(currentMap);
        int width = getGUIWidth(map);
        int height = getGUIHeight(map);
        moveCursorTo(0,height+1);
        for (int i = 1; i <= NUMBER_OF_INTERACTION_COMMANDS; i++) {
            clearLine();
            moveCursorTo(0,height+1+i);
        }
        moveCursorTo(0,height+1);
    }

    /**
     * Renvoie une chaine dans laquelle une autre chaine a été répétée autant de fois que précisé.
     * @param c La chaine à répéter.
     * @param times Le nombre de fois que la chaîne doit être répétée.
     * @return Une nouvelle chaine contenant le caractère `c` répété `times` fois.
     */
    String repeat(String c, int times) {
        String str = "";
        for (int i = 0; i < times; i++) {
            str += c;
        }
        return str;
    }

    /**
     * Affiche une ligne dans laquelle il y a un certain nombre de fois le signe "=".
     * @param times Le nombre de "=".
     */
    void printEqualsRow(int times) {
        for (int i = 0; i < times; i++) {
            print("=");
        }
        println("");
    }
    
    /**
     * Saute une ligne pour créer un effet d'espacement entre du texte.
     * @param times Le nombre de lignes vides.
     */
    void printEmptyLines(int times) {
        for (int i = 0; i < times; i++) {
            println("");
        }
    }

    /**
     * Saute une seule ligne pour créer un léger effet d'espacement entre du texte.
     */
    void printEmptyLine() {
        println("");
    }

    /**
     * Colore un pixel.
     * @param color La couleur à utiliser pour colorer le pixel
     */
    void printPixel(Color color) {
        print(color.ANSI + PIXEL + ANSI_RESET);
    }

    /**
     * Colore un pixel qui ne provient pas d'une carte.
     * C'est le cas pour le pixel du joueur.
     * @param color La couleur ANSI.
     */
    void printPixel(String color) {
        print(color + PIXEL + ANSI_RESET);
    }

    /**
     * Affiche le pixel correspondant au joueur.
     */
    void printPlayer() {
        printPixel("\u001b[48;2;255;204;187m");
    }

    /**
     * On affiche le joueur avec des coordonnées précisées.
     * Ceci est utile s'il n'est pas nécessaire de vérifier 
     * si les coordonnées sont valides ou non.
     * @param x La coordonnée en X
     * @param y La coordonnée en Y
     */
    void printPlayer(int x, int y) {
        saveCursorPosition();
        moveCursorTo(x*PIXEL_SIZE+getXShift(), y+getYShift());
        printPlayer();
        restoreCursorPosition();
    }

    /**
     * Colore un pixel transparent
     */
    void printTransparentPixel() {
        print(ANSI_BG_DEFAULT_COLOR + PIXEL + ANSI_RESET);
    }

    /**
     * Affiche un texte sur la ligne courant en gras.
     */
    void printBoldText(String text) {
        print(makeBoldText(text));
    }

    /**
     * Retourne le texte en gras à afficher.
     */
    String makeBoldText(String text) {
        return ANSI_BOLD + text + ANSI_RESET;
    }

    /**
     * Affiche un texte au milieu de l'écran, selon la largeur totale donnée.
     * Pour calculer la bonne position en fonction de la largeur du texte,
     * il suffit de répéter des " " x fois avant le texte, tel que x = (largeur totale / 2) - (longueur du texte / 2),
     * où la longueur du texte est calculée de manière à retirer les codes ANSI potentiels au début et fin de chaine.
     * @param text Le texte à afficher
     * @param totalWidth La largeur totale de la fenêtre
     */
    void printlnCenterAlignedText(String text, int totalWidth) {
        println(repeat(" ", totalWidth/2-length(text.replaceAll("\\033\\[.*m(.+)\\033\\[.*m", "$1"))/2) + text);
    }

    /**
     * Affiche du texte aligné sur la droite.
     * Pour cela, on affiche des espaces sur le côté gauche jusqu'au côté droit - la taille du texte.
     * @param text Le texte à afficher
     * @param maxWidth La largeur maximale de l'interface (la limite du côté droit)
     */
    void printlnRightAlinedText(String text, int maxWidth) {
        println(repeat(" ", maxWidth - length(text)) + text);
    }

    /**
     * Affiche une image ASCII depuis un fichier texte.
     * @param path Le chemin vers le fichier texte.
     * @return Un entier qui correspond au nombre de lignes affichées.
     */
    int printASCII(String path) {
        File text = newFile(path);
        int i = 0;
        while (ready(text)) {
            println(readLine(text));
            i++;
        }
        return i;
    }

    /**
     * Retourne le contenu d'un fichier texte.
     * Ceci est utilisé pour afficher une image ASCII.
     * @param path Le chemin vers le fichier.
     * @return Le contenu du fichier.
     */
    String readASCII(String path) {
        File file = newFile(path);
        String output = "";
        while (ready(file)) {
            output+=readLine(file)+"\n";
        }
        return output;
    }

    /**
     * Affiche un message venant de la télé.
     * @param message Le message textuel à afficher sur une ligne.
     */
    void writeMessage(String message) {
        Map map = getMapOfName(currentMap);
        int height = getGUIHeight(map) + (EVASION_STARTED ? 0 : NUMBER_OF_INTERACTION_COMMANDS);
        moveCursorTo(0,height+2);
        clearLine();
        println(message);
    }

    /**
     * Affiche le message d'un dialogue.
     */
    void writeDialog(Dialog dialog) {
        if (length(dialog.narratorName) == 0) {
            writeMessage(dialog.text);
        } else {
            writeMessage(dialog.narratorName + " - " + dialog.text);    
        }
    }

    /**
     * Affiche une aide du jeu, en passant ça comme une aide de Baste.
     */
    void writeHelp(Help help) {
        writeMessage("Baste - " + help.text);
    }

    /**
     * Retourne l'aide de Baste divisé en plusieurs messages pour que ce soit plus ergonomique.
     * @param g Le numéro du groupe auquel appartiennent les messages à afficher, dans l'ordre.
     * @return L'aide de Baste.
     */
    Help[] getHelpOfGroup(int g) {
        Help[] help = new Help[20]; // on devrait faire `ArrayList<Help> help = new ArrayList<>();` c'est honteux
        int y = 0;
        for (int i = 0; i < length(HELP); i++) {
            if (HELP[i].group == g) {
                help[y++] = HELP[i];
            }
        }
        Help[] copy = new Help[y];
        for (int i = 0; i < y; i++) {
            copy[i] = help[i];
        }
        return copy;
    }

    /**
     * Affiche le dernier message de Baste.
     * Celui-ci devra rester afficher sur l'écran à tout moment durant l'évasion.
     */
    void printEvasionHelp() {
        writeMessage("Baste - Dépêche-toi ! La sortie se trouve vers le haut du couloir 6 !");
    }

    /*
     *
     * C'est le moment de parler d'une dinguerie...
     * En bref, la façon dont fonctionne `enableKeyTypedInConsole` c'est de changer
     * le mode d'entrée des commandes du terminal via `stty raw`.
     * Le problème n'est pas très clair, mais en gros, quand on fait un clearScreen(),
     * tout en étant en mode `raw`, les "carriage return" sont oubliés (\r) lors de l'ajout d'un '\n', et par conséquent,
     * on obtient le "staircase effect" comme décrit ci-dessous.
     * Nous avons donc manuellement ajouté le '\r' aux méthodes `println`.
     *
     * Exemple pour le "staircaise effect" causé par le `println` de iJava (un `println` par ligne) :
     * ```
     * hello
     *      my
     *        name
     *            is
     *              JOHN
     *                  CENA
     * ```
     *
     * Plus d'info ici : https://unix.stackexchange.com/a/366426
     *
     */

    void println(String str) {
        print("\r" + str + "\r\n");
    }

    void println(int a) {
        print("\r" + a + "\r\n");
    }
}
