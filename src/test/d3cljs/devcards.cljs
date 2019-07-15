(ns d3cljs.devcards
  (:require
   [devcards.core :as dc :include-macros true]
   [reagent.core :as reagent]))

(dc/defcard-rg sample-card
  [:div
   [:h1 "My first devcard"]
   [:div "See the following links for details:"
    [:ul
     [:li [:a {:href "https://github.com/bhauman/devcards"} "DevCards home"]]
     [:li [:a {:href "http://rigsomelight.com/devcards/#!/devdemos.reagent"} "DevCard with reagent"]]]]])

(defn ^:export init []
  (dc/start-devcard-ui!))
