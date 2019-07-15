(ns cljsjs.marked
  (:require ["marked" :as m]))

(js/goog.exportSymbol "marked" m)
