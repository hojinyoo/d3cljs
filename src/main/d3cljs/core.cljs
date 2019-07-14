(ns d3cljs.core
  (:require-macros [d3cljs.util :as util])
  (:require
   [cljs.spec.alpha :as spec]
   ;; [applied-science.js-interop :as j]
   [d3cljs.basic :as basic]
   [reagent.core :as reagent]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn page []
  [:div
   [:p "D3 test page"]
   [:section
    [:h2 [:strong "Basics"]]
    [:h3 [:code ":elem"] " piece"]
    [basic/test-1 {}]
    [basic/test-2 {}]]])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")
    (spec/check-asserts true)))

(defn reload []
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (reload))
