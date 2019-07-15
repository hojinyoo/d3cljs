(ns d3cljs.simulation)


;; selected_node = null,
;; selected_link = null,
;; mousedown_link = null,
;; mousedown_node = null,
;; mouseup_node = null;
(def state (atom {}))

(declare vis)
(defn rescale
  "rescale g"
  []
  (let [trans d3/event.translate
        scale d3/event.scale]
    (.. vis
        (attr "transform" (str "translate(" trans ") scale(" scale ")")))))

(def zoom (.. d3
              (zoom)
              (on "zoom" rescale)))

(def outer (.. app
               (append "svg:svg")
               (attr "width" width)
               (attr "height" height)ㅠ
               (attr "pointer-events" "all")))

(def vis (.. outer
             (append "svg:g")
             (call zoom)
             (on "dblclick.zoom" nil)
             (append "svg:g")))

(.. vis
    (append "svg:rect")
    (attr "width" width)
    (attr "height" height)
    (attr "fill" "white"))

(declare tick)
(def force (.. (d3/forceSimulation)
               #_(size #js [width height])
               (nodes #js [ #js {}])
               (linkDistance 50)
               (charge -200)
               (on "tick" tick)))

(def drag-line (.. vis
                   (append "line")
                   (attr "class" "drag_line")
                   (attr "x1" 0)
                   (attr "y1" 0)
                   (attr "x2" 0)
                   (attr "y2" 0)))

(def nodes (.nodes force))
(def links (.links force))

(def node (.. vis
              (selectAll ".node")))
(def link (.. vis
              (selectAll ".link")))
(declare keydown)
#_(.. d3
      (select js/window)
      (on "keydown" keydown))

(declare redraw)
#_(redraw)

(defn mousedown []
  (let [{:keys [mousedown-node mousedown-link]} @state]
    (when-not (or mousedown-node mousedown-link)
      (.. vis
          (call zoom)))))

(defn mousemove []
  (let [{:keys [mousedown-node]} @state]
    (when mousedown-node
      (this-as this
        (.. drag-line
            (attr "x1" (.-x mousedown-node))
            (attr "y1" (.-y mousedown-node))
            (attr "x2" (aget (d3/mouse this) 0))
            (attr "y2" (aget (d3/mouse this) 1)))))))

(declare reset-mouse-vars)
(defn mouseup []
  (let [{:keys [mousedown-node mouseup-node]} @state]
    (when mousedown-node
      (this-as this
        (.. drag-line
            (attr "class" "drag_line_hidden"))
        (when-not mouseup-node
          (let [point (d3/mouse this)
                node #js {:x (aget point 0)
                          :y (aget point 1)}]
            (.. nodes
                (push node))
            (swap! state assoc :selected-node node)
            (swap! state assoc :selected-link nil)
            (.. links
                (push #js {:source mousedown-node :target node}))))
        (redraw)))
    (reset-mouse-vars)))

(defn reset-mouse-vars []
  (swap! state assoc :mousedown-node nil)
  (swap! state assoc :mouseup-node nil)
  (swap! state assoc :mousedown-link nil))

(defn tick []
  (.. link
      (attr "x1" (fn [d] (.. d -source -x)))
      (attr "y1" (fn [d] (.. d -source -y)))
      (attr "x2" (fn [d] (.. d -target -x)))
      (attr "y2" (fn [d] (.. d -target -y))))
  (.. node
      (attr "cx" (fn [d] (.. d -x)))
      (attr "cy" (fn [d] (.. d -y)))))

#_(defn redraw []
    (let [link- (.. link (data links))]
      (.. link-
          (enter)
          (insert "line" ".node")
          (attr "class" "link")
          (on "mousedown" (fn [d]
                            (swap! state :mousedown-link d)
                            (let [{:keys [mousedown-link selected-link]} @state]
                              (if (= mousedown-link selected-link)
                                (swap! state assoc :selected-link nil)
                                (swap! state assoc :selected-link mousedown-link))
                              (swap! state assoc :selected-link nil))
                            (redraw))))
      (.. link-
          (exit)
          (remove))
      (.. link-
          (classed "link_selected" (fn [d] (= d (:selected-link @state))))))
    (let [node- (.. node (data nodes))]
      (.. node-
          (enter)
          (insert "circle")
          (attr "class" "node")
          (attr "r" 5)
          (on "mousedown"
              (fn [d]
                (.. vis (call zoom nil))
                (swap! state assoc :mousedown-node d)
                (let [{:keys [mousedown-node selected-node]} @state]
                  (if (= mousedown-node selected-node)
                    (swap! state assoc :selected-node nil)
                    (swap! state assoc :selected-node mousedown-node))
                  (swap! state assoc :selected-link nil)
                  (.. drag-line
                      (attr "class" "link")
                      (attr "x1" (.-x mousedown-node))
                      (attr "y1" (.-y mousedown-node))
                      (attr "x2" (.-x mousedown-node))
                      (attr "y2" (.-y mousedown-node)))
                  (redraw))))
          (on "mousedrag" (fn [d]))
          (on "mouseup"
              (fn [d]
                (let [{:keys [mousedown-node mouseup-node]} @state]
                  (swap! state assoc :mouseup-node d)
                  (if (= mouseup-node mousedown-node)
                    (reset-mouse-vars)))
                (let [{:keys [mousedown-node mouseup-node]} @state
                      link-- #js {:source mousedown-node :target mouseup-node}]
                  (.push links link)
                  (swap! state assoc :selected-link link)
                  (swap! state assoc :selected-node nil)
                  (.. vis (call zoom rescale)))))
          (transition)
          (duration 750)
          (ease "elastic")
          (attr "r" 6.5))
      (.. node-
          (exit)
          (transition)
          (attr "r" 0)
          (remove))
      (.. node-
          (classed "node_selected" (fn [d] (= d (:selected-node @state))))))
    (when d3/event
      (.preventDefault d3/event))
    (.start force))

#_(defn splice-links-for-node [node]
    (let [to-splice (.filter links (fn [l] (or (= (.-source l) node) (= (.-target l) node))))]
      (.map to-splice (fn [l] (.splice links (.indexOf links l) 1)))))

#_(defn keydown []
    (let [{:keys [selected-node selected-link]} @state]
      (when (or selected-node selected-link)
        (case (.-keyCode d3/event.)
          (8 46) (do
                   (cond
                     selected-node (do
                                     (.splice nodes (.. nodes (.indexOf selected-node)) 1)
                                     (splice-links-for-node selected-node))
                     selected-link (.splice links (.. links indexOf selected-link) 1))
                   (swap! state
                          assoc
                          :selected-link nil
                          :selected-node nil)
                   (redraw))))))
