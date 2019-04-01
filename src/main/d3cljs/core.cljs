(ns d3cljs.core
  (:refer-clojure :exclude [force remove])
  (:require-macros [d3cljs.util :as util])
  (:require
   ["d3" :as d3]
   [applied-science.js-interop :as j]
   [cljs.spec.alpha :as s]
   [clojure.test.check.generators :as gen]))

(def app (.. d3 (select "#app")))
(def width 600)
(def height 600)
(def fill (d3/scaleOrdinal d3/schemeCategory10))

(defonce data (-> "https://gist.githubusercontent.com/mbostock/4062045/raw/5916d145c8c048a6e3086915a6be464467391c62/miserables.json"
                  util/slurp
                  js/JSON.parse
                  (js->clj :keywordize-keys true)))

(defonce svg (.. app
                 (append "svg:svg")
                 (attr "width" width)
                 (attr "height" height)))

(defn drag- [sim]
  (let [drag-started (fn [d]
                       (let [active (j/get-in d3 [:event :active])]
                         (when-not ^boolean active
                           (.. sim
                               (alphaTarget 0.3)
                               (restart))))
                       (let [{:keys [x y]} (j/lookup d)]
                         (j/assoc! d :fx x :fy y)))
        dragged (fn [d]
                  (let [{:keys [x y]} (j/lookup (j/get d3 :event))]
                    (j/assoc! d :fx x :fy y)))
        drag-ended (fn [d]
                     (let [active (j/get-in d3 [:event :active])]
                       (when-not ^boolean active
                         (.. sim
                             (alphaTarget 0))))
                     (j/assoc! d :fx nil :fy nil))]
    (.. d3
        (drag)
        (on "start" drag-started)
        (on "drag" dragged)
        (on "end" drag-ended))))

(defn ->chart [svg]
  (let [nodes (clj->js (:nodes data))
        links (clj->js (:links data))
        force-link (.. d3
                       (forceLink links)
                       (id #(j/get % :id)))
        simulation (.. d3
                       (forceSimulation nodes)
                       (force "link" force-link)
                       (force "charge" (.forceManyBody d3))
                       (force "center" (.forceCenter d3 (/ width 2) (/ height 2))))
        link (.. svg
                 (append "g")
                 (attr "stroke" "#999")
                 (attr "stroke-opacity" 0.6)
                 (selectAll "line")
                 (data links)
                 (join "line")
                 (attr "stroke-width" #(Math/sqrt (j/get % :value))))
        node (.. svg
                 (append "g")
                 (attr "stroke" "#fff")
                 (attr "stroke-width" 1.5)
                 (selectAll "circle")
                 (data nodes)
                 (join "circle")
                 (attr "r" 5)
                 (attr "fill" #(fill (j/get % :group)))
                 (call (drag- simulation)))]
    (.. node
        (append "title")
        (text #(j/get % :id)))
    (.. simulation
        (on "tick" (fn []
                     (.. link
                         (attr "x1" #(j/get-in % [:source :x]))
                         (attr "y1" #(j/get-in % [:source :y]))
                         (attr "x2" #(j/get-in % [:target :x]))
                         (attr "y2" #(j/get-in % [:target :y])))
                     (.. node
                         (attr "cx" #(j/get % :x))
                         (attr "cy" #(j/get % :y))))))
    (.. svg (node))))

(defn ^:export go []
  (prn "start!")
  (->chart svg))
