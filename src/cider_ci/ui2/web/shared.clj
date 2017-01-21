; Copyright © 2016 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.ui2.web.shared
  (:refer-clojure :exclude [str keyword])
  (:require [cider-ci.utils.core :refer [keyword str]])

  (:require
    [cider-ci.ui2.constants :refer [CONTEXT]]

    [cider-ci.utils.rdbms :as rdbms]
    [cider-ci.utils.self]

    [clojure.java.jdbc :as jdbc]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug :refer [I> I>> identity-with-logging]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
    ))

(defn admins? []
  (->> ["SELECT true AS exists FROM users
        WHERE is_admin = true limit 1"]
       (jdbc/query (rdbms/get-ds))
       first
       :exists
       boolean))



;#### debug ###################################################################

;(debug/debug-ns *ns*)

