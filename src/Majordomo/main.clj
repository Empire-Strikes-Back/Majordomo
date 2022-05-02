(ns Majordomo.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [clojure.repl :as Wichita.repl]
   [cljfmt.core :as Joker.core]

   [Majordomo.drawing]
   [Majordomo.seed]
   [Majordomo.raisins]
   [Majordomo.peanuts]
   [Majordomo.oranges]
   [Majordomo.salt]
   [Majordomo.microwaved-potatoes]
   [Majordomo.corn]
   [Majordomo.beans])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JTextPane)
   (javax.swing.border EmptyBorder)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo UIScale)
   (java.util.function Consumer)
   (java.util ServiceLoader)
   (org.kordamp.ikonli Ikon)
   (org.kordamp.ikonli IkonProvider)
   (org.kordamp.ikonli.swing FontIcon)
   (org.kordamp.ikonli.codicons Codicons)
   (net.miginfocom.swing MigLayout)
   (net.miginfocom.layout ConstraintParser LC UnitValue)
   (java.io File)
   (java.lang Runnable))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce gamesA (atom nil))
(defonce gameA (atom nil))
(defonce resize| (chan (sliding-buffer 1)))
(defonce eval| (chan 10))
(defonce cancel-sub| (chan 1))
(defonce cancel-pub| (chan 1))
(defonce ops| (chan 10))
(defonce table| (chan (sliding-buffer 10)))
(defonce sub| (chan (sliding-buffer 10)))
(def ^:dynamic ^JFrame jframe nil)
#_(defonce *ns (find-ns 'Majordomo.main))

(def ^:const jframe-title "it was a scheduled vacation, actually")



(defn reload
  []
  (require
   '[Majordomo.seed]
   '[Majordomo.raisins]
   '[Majordomo.peanuts]
   '[Majordomo.oranges]
   '[Majordomo.salt]
   '[Majordomo.microwaved-potatoes]
   '[Majordomo.corn]
   '[Majordomo.beans]
   '[Majordomo.main]
   :reload))

(defn -main
  [& args]
  (println "i dont want my next job")

  #_(alter-var-root #'*ns* (constantly (find-ns 'Majordomo.main)))

  (when SystemInfo/isMacOS
    (System/setProperty "apple.laf.useScreenMenuBar" "true")
    (System/setProperty "apple.awt.application.name" jframe-title)
    (System/setProperty "apple.awt.application.appearance" "system"))

  (when SystemInfo/isLinux
    (JFrame/setDefaultLookAndFeelDecorated true)
    (JDialog/setDefaultLookAndFeelDecorated true))

  (when (and
         (not SystemInfo/isJava_9_orLater)
         (= (System/getProperty "flatlaf.uiScale") nil))
    (System/setProperty "flatlaf.uiScale" "2x"))

  (FlatLightLaf/setup)

  (FlatDesktop/setQuitHandler (reify Consumer
                                (accept [_ response]
                                  (.performQuit ^FlatDesktop$QuitResponse response))
                                (andThen [_ after] after)))

  (let [screenshotsMode? (Boolean/parseBoolean (System/getProperty "flatlaf.demo.screenshotsMode"))

        jframe (JFrame. jframe-title)
        jmenubar (JMenuBar.)
        jtoolbar (JToolBar.)
        root-panel (JPanel.)]

    (let [data-dir-path (or
                         (some-> (System/getenv "MAJORDOMO_PATH")
                                 (.replaceFirst "^~" (System/getProperty "user.home")))
                         (.getCanonicalPath ^File (Wichita.java.io/file (System/getProperty "user.home") "Majordomo")))
          state-file-path (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Majordomo.edn"))]
      (Wichita.java.io/make-parents data-dir-path)
      (reset! stateA {})
      (reset! gamesA {})
      (reset! gameA {}))

    (SwingUtilities/invokeLater
     (reify Runnable
       (run [_]

         (doto jframe
           (.add root-panel)
           (.addComponentListener (let []
                                    (reify ComponentListener
                                      (componentHidden [_ event])
                                      (componentMoved [_ event])
                                      (componentResized [_ event] (put! resize| (.getTime (java.util.Date.))))
                                      (componentShown [_ event])))))

         (doto root-panel
           #_(.setLayout (BoxLayout. root-panel BoxLayout/Y_AXIS))
           (.setLayout (MigLayout. "insets 10"
                                   "[grow,shrink,fill]"
                                   "[grow,shrink,fill]")))

         (when-let [url (Wichita.java.io/resource "icon.png")]
           (.setIconImage jframe (.getImage (ImageIcon. url))))

         (Majordomo.microwaved-potatoes/menubar-process
          {:jmenubar jmenubar
           :jframe jframe
           :menubar| ops|})
         (.setJMenuBar jframe jmenubar)

         #_(Majordomo.microwaved-potatoes/toolbar-process
            {:jtoolbar jtoolbar})
         #_(.add root-panel jtoolbar "dock north")

         (.setPreferredSize jframe
                            (let [size (-> (Toolkit/getDefaultToolkit) (.getScreenSize))]
                              (Dimension. (UIScale/scale 1024) (UIScale/scale 576)))
                            #_(if SystemInfo/isJava_9_orLater
                                (Dimension. 830 440)
                                (Dimension. 1660 880)))

         #_(doto jframe
             (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
             (.setSize 2400 1600)
             (.setLocation 1300 200)
             #_(.add panel)
             (.setVisible true))

         #_(println :before (.getGraphics canvas))
         (doto jframe
           (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
           (.pack)
           (.setLocationRelativeTo nil)
           (.setVisible true))
         #_(println :after (.getGraphics canvas))

         (alter-var-root #'Majordomo.main/jframe (constantly jframe))

         (remove-watch stateA :watch-fn)
         (add-watch stateA :watch-fn
                    (fn [ref wathc-key old-state new-state]

                      (when (not= old-state new-state)
                        (do)))))))

    (go
      (loop []
        (when-let [{:keys [message from] :as value} (<! sub|)]
          (condp = (:op message)
            :game-state
            (let [{:keys [game-state]} message]
              (swap! gameA merge game-state))
            :player-state
            (let [{:keys [game-state]} message]
              (swap! gameA update-in [:players from] merge message))
            :games
            (let [{:keys [frequency host-peer-id]} message]
              (swap! gamesA update-in [frequency] merge message)))
          (recur))))

    (go
      (loop []
        (<! (timeout 3000))
        (let [expired (into []
                            (comp
                             (keep (fn [[frequency {:keys [timestamp]}]]
                                     #_(println (- (.getTime (java.util.Date.)) timestamp))
                                     (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                       frequency))))
                            @gamesA)]
          (when-not (empty? expired)
            (apply swap! gamesA dissoc expired)))
        (recur)))

    (go
      (loop []
        (<! (timeout 3000))
        (let [expired (into []
                            (comp
                             (keep (fn [[frequency {:keys [timestamp peer-id]}]]
                                     #_(println (- (.getTime (java.util.Date.)) timestamp))
                                     (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                       frequency))))
                            (:players @gameA))]
          (when-not (empty? expired)
            (apply swap! gameA update :players dissoc expired)))
        (recur)))

    (go
      (loop []
        (when-let [value (<! ops|)]
          (condp = (:op value)
            :game
            (let [{:keys [frequency role]} value
                  id| (chan 1)
                  port (or (System/getenv "MAJORDOMO_IPFS_PORT") "5001")
                  ipfs-api-url (format "http://127.0.0.1:%s" port)
                  games-topic (Majordomo.corn/encode-base64url-u "raisins")
                  game-topic (Majordomo.corn/encode-base64url-u frequency)
                  _ (Majordomo.corn/subscribe-process
                     {:sub| sub|
                      :cancel| cancel-sub|
                      :frequency frequency
                      :ipfs-api-url ipfs-api-url
                      :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
                      :id| id|})
                  host? (= role :host)
                  {:keys [peer-id]} (<! id|)]
              #_(println :game value)
              (go
                (loop []
                  (alt!
                    cancel-pub|
                    ([_] (do nil))

                    (timeout 2000)
                    ([_]
                     (when host?
                       (Majordomo.corn/pubsub-pub
                        ipfs-api-url games-topic (str {:op :games
                                                       :timestamp (.getTime (java.util.Date.))
                                                       :frequency frequency
                                                       :host-peer-id peer-id}))
                       (Majordomo.corn/pubsub-pub
                        ipfs-api-url game-topic (str {:op :game-state
                                                      :timestamp (.getTime (java.util.Date.))
                                                      :game-state {:host-peer-id peer-id}})))

                     (Majordomo.corn/pubsub-pub
                      ipfs-api-url game-topic (str {:op :player-state
                                                    :timestamp (.getTime (java.util.Date.))
                                                    :peer-id peer-id}))
                     (recur))))))

            :leave
            (let [{:keys [frequency]} value]
              (>! cancel-sub| true)
              (>! cancel-pub| true)
              (reset! gameA {}))

            :discover
            (let [discover-jframe (JFrame. "discover")]
              (Majordomo.microwaved-potatoes/discover-process
               {:jframe discover-jframe
                :root-jframe jframe
                :ops| ops|
                :gamesA gamesA
                :gameA gameA
                :stateA stateA})
              (reset! gameA @gameA))

            :host-yes
            (let [{:keys [frequency]} value]
              (println :frequency frequency)))

          (recur))))

    (let [port (or (System/getenv "MAJORDOMO_IPFS_PORT") "5001")
          ipfs-api-url (format "http://127.0.0.1:%s" port)
          id| (chan 1)]
      (Majordomo.corn/subscribe-process
       {:sub| sub|
        :cancel| (chan (sliding-buffer 1))
        :frequency "raisins"
        :ipfs-api-url ipfs-api-url
        :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
        :id| id|})))
  (println "Kuiil has spoken"))