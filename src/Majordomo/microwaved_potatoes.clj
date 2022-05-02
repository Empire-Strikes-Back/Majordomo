(ns Majordomo.microwaved-potatoes
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [Majordomo.seed])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTextPane JTextField JTable)
   (javax.swing DefaultListSelectionModel)
   (javax.swing.border EmptyBorder)
   (javax.swing.table DefaultTableModel)
   (javax.swing.event DocumentListener DocumentEvent ListSelectionListener ListSelectionEvent)
   (javax.swing.text SimpleAttributeSet StyleConstants JTextComponent)
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
   (java.io ByteArrayOutputStream)
   (java.lang Runnable)

   (java.awt.image BufferedImage)
   (java.awt Image Graphics2D Color)
   (javax.imageio ImageIO)
   (java.security MessageDigest)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn menubar-process
  [{:keys [^JMenuBar jmenubar
           ^JFrame jframe
           menubar|]
    :as opts}]
  (let [on-menubar-item (fn [f]
                          (reify ActionListener
                            (actionPerformed [_ event]
                              (SwingUtilities/invokeLater
                               (reify Runnable
                                 (run [_]
                                   (f _ event)))))))

        on-menu-item-show-dialog (on-menubar-item (fn [_ event] (JOptionPane/showMessageDialog jframe (.getActionCommand ^ActionEvent event) "menu bar item" JOptionPane/PLAIN_MESSAGE)))]
    (doto jmenubar
      (.add (doto (JMenu.)
              (.setText "program")
              (.setMnemonic \F)
              (.add (doto (JMenuItem.)
                      (.setText "game")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_H (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \H)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :game}))))))
              #_(.add (doto (JMenuItem.)
                        (.setText "join")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_J (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \J)
                        (.addActionListener on-menu-item-show-dialog)))
              #_(.add (doto (JMenuItem.)
                        (.setText "observe")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_O (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "discover")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_D (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \D)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :discover}))))))
              (.add (doto (JMenuItem.)
                      (.setText "settings")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_S (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \S)
                      (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "exit")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \Q)
                      (.addActionListener (on-menubar-item (fn [_ event]
                                                             (.dispose jframe))))))))

      #_(.add (doto (JMenu.)
                (.setText "edit")
                (.setMnemonic \E)
                (.add (doto (JMenuItem.)
                        (.setText "undo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Z (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "redo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Y (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \R)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "cut")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_X (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \C)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "copy")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_C (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "paste")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_V (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \P)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "delete")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_DELETE 0))
                        (.setMnemonic \D)
                        (.addActionListener on-menu-item-show-dialog)))))))
  nil)

(defn toolbar-process
  [{:keys [^JToolBar jtoolbar]
    :as opts}]
  (let []
    (doto jtoolbar
      #_(.setMargin (Insets. 3 3 3 3))
      (.add (doto (JButton.)
              (.setToolTipText "new file")
              (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/NEW_FILE (UIScale/scale 16) Color/BLACK))))
      (.add (doto (JButton.)
              (.setToolTipText "open file")
              (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/FOLDER_OPENED (UIScale/scale 16) Color/BLACK))))
      (.add (doto (JButton.)
              (.setToolTipText "save")
              (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/SAVE (UIScale/scale 16) Color/BLACK))))
      (.add (doto (JButton.)
              (.setToolTipText "undo")
              (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/DISCARD (UIScale/scale 16) Color/BLACK))))
      (.add (doto (JButton.)
              (.setToolTipText "redo")
              (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/REDO (UIScale/scale 16) Color/BLACK))))
      #_(.addSeparator)))
  nil)

(defn host-game-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ^String frequency
           yes|]
    :or {frequency (str (java.util.UUID/randomUUID))}
    :as opts}]
  (let [root-panel (JPanel.)
        jfrequency-text-field (JTextField. frequency 40)
        jbutton-yes (JButton. "yes")]

    (doto jframe
      (.add root-panel))

    (doto jbutton-yes
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! yes| {:op :host-yes
                       :frequency (.getText jfrequency-text-field)})
           (.dispose jframe)))))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add (JLabel. "frequency") "cell 0 0")
      (.add jfrequency-text-field "cell 1 0")
      (.add jbutton-yes "cell 0 1"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(defn discover-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ops|
           gamesA
           gameA
           stateA]
    :or {}
    :as opts}]
  (let [root-panel (JPanel.)
        jtable (JTable.)
        jscroll-pane (JScrollPane.)

        jbutton-host (JButton. "host")
        jbutton-join (JButton. "join")
        jbutton-observe (JButton. "observe")
        jbutton-leave (JButton. "leave")
        jbutton-open (JButton. "open")

        jtext-field-frequency (JTextField. (str (java.util.UUID/randomUUID)) 40)

        column-names (into-array ^Object ["frequency" "host"])
        table-model (DefaultTableModel.) #_(DefaultTableModel.
                                            ^"[[Ljava.lang.Object;"
                                            (to-array-2d
                                             [[(str (java.util.UUID/randomUUID)) 10]
                                              [(str (java.util.UUID/randomUUID)) 10]])
                                            ^"[Ljava.lang.Object;"
                                            (into-array ^Object ["frequency" "guests"])
                                            #_(object-array
                                               [(object-array)
                                                (object-array
                                                 [(str (java.util.UUID/randomUUID)) 10])]))]

    (doto jframe
      (.add root-panel))

    (doto jtable
      (.setModel table-model)
      (.setRowSelectionAllowed true)
      (.setSelectionModel (doto (DefaultListSelectionModel.)
                            (.addListSelectionListener
                             (reify ListSelectionListener
                               (valueChanged [_ event]
                                 (when (not= -1 (.getSelectedRow jtable))
                                   (SwingUtilities/invokeLater
                                    (reify Runnable
                                      (run [_]
                                        (.setText jtext-field-frequency (.getValueAt jtable (.getSelectedRow jtable) 0)))))))))))
      #_(.setAutoCreateRowSorter true))

    (doto jscroll-pane
      (.setViewportView jtable)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jbutton-host
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :game
                       :role :host
                       :frequency (.getText jtext-field-frequency)})
           #_(.dispose jframe)))))

    (doto jbutton-join
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :game
                       :role :player
                       :frequency (.getText jtext-field-frequency)})
           #_(.dispose jframe)))))

    (doto jbutton-leave
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :leave})
           #_(.dispose jframe)))))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add jscroll-pane "cell 0 0 3 1, width 100%")
      (.add jtext-field-frequency "cell 0 1")
      (.add jbutton-host "cell 0 2")
      (.add jbutton-join "cell 0 2")
      (.add jbutton-open "cell 0 2")
      (.add jbutton-leave "cell 0 2"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (remove-watch gameA :discover-process)
    (add-watch gameA :discover-process
               (fn [ref wathc-key old-state new-state]
                 #_(println  :gameA old-state new-state)
                 #_(when (not= old-state new-state))
                 (SwingUtilities/invokeLater
                  (reify Runnable
                    (run [_]
                      (let [we-host? (= (:host-id new-state) (:peer-id @stateA))
                            in-game? (not (empty? new-state))]
                        (.setEnabled jbutton-open in-game?)
                        (.setEnabled jbutton-leave in-game?)
                        (.setEnabled jbutton-host (not in-game?))
                        (.setEnabled jbutton-join (not in-game?))))))))

    (remove-watch gamesA :discover-process)
    (add-watch gamesA :discover-process
               (fn [ref wathc-key old-state new-state]
                 (when (not= old-state new-state)
                   #_(println new-state)
                   (SwingUtilities/invokeLater
                    (reify Runnable
                      (run [_]
                        (let [selected-frequency (when (not= -1 (.getSelectedRow jtable))
                                                   (.getValueAt jtable (.getSelectedRow jtable) 0))
                              data (map (fn [[frequency {:keys [frequency host-peer-id]}]]
                                          [frequency host-peer-id]) new-state)]
                          (.setDataVector table-model
                                          ^"[[Ljava.lang.Object;"
                                          #_(to-array-2d
                                             [[(str (java.util.UUID/randomUUID)) 10]
                                              [(str (java.util.UUID/randomUUID)) 10]])
                                          (to-array-2d data)
                                          ^"[Ljava.lang.Object;"
                                          column-names)
                          (when selected-frequency
                            (let [^int new-index (->>
                                                  data
                                                  (into []
                                                        (comp
                                                         (map first)
                                                         (map-indexed vector)
                                                         (keep (fn [[index frequency]] (when (= frequency selected-frequency) index)))))
                                                  (first))]
                              (when new-index
                                (.setRowSelectionInterval jtable new-index new-index)))))))))))

    #_(go
        (loop []
          (when-let [value (<! table|)]
            (SwingUtilities/invokeLater
             (reify Runnable
               (run [_]
                 (.setDataVector table-model
                                 ^"[[Ljava.lang.Object;"
                                 (to-array-2d
                                  value)
                                 ^"[Ljava.lang.Object;"
                                 column-names))))
            (recur))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(comment

  (.getName (class (make-array Object 1 1)))

  (.getName (class (make-array String 1)))

  ;
  )