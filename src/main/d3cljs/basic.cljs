(ns d3cljs.basic
  (:require
   [reagent.core :as reagent]
   [rid3.core :as rid3 :refer [rid3->]]))

(def height 100)
(def width 100)

(defn test-1 [app-state]
  (let [viz-ratom (reagent/atom {})]
    (fn [app-state]
      [:div
       [:h4 "1) Add a simple element: a rect"]

       [rid3/viz
        {:id    "b01"
         :ratom viz-ratom

         ;; This is required to, at a minimum, set the dimensions of
         ;; your svg.  Think of your `svg` as a whiteboard that you are
         ;; going to draw stuff on
         :svg {:did-mount (fn [node ratom]
                            (rid3-> node
                                    {:height height
                                     :width  width}))}

         ;; Think of pieces as the things you are drawing on your
         ;; whiteboard.
         :pieces
         [{:kind      :elem
           :class     "some-element"
           :tag       "rect"
           :did-mount (fn [node ratom]
                        (rid3-> node
                                {:x      0
                                 :y      0
                                 :height height
                                 :width  width}))}
          ]
         }]])))

(defn test-2 [app-state]
  (let [viz-ratom (reagent/atom {})]
    (fn [app-state]
      [:div
       [:h4 "2) Add attribute to the rect (fill)"]

       [rid3/viz
        {:id    "b02"
         :ratom viz-ratom

         :svg {:did-mount (fn [node ratom]
                            (rid3-> node
                                    {:height height
                                     :width  width}))}

         :pieces
         [{:kind      :elem
           :class     "some-element"
           :tag       "rect"
           :did-mount (fn [node ratom]
                        (rid3-> node
                                {:x            0
                                 :y            0
                                 :height       height
                                 :width        width
                                 ;; You can add arbitrary attributes to
                                 ;; your element
                                 :fill         "lightgrey"}))}
          ]
         }]])))
