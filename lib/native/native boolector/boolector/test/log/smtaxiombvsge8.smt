(benchmark smtaxiombvsge
 :logic QF_BV
 :extrafuns ((s BitVec[8]))
 :extrafuns ((t BitVec[8]))
 :formula (not (=
    (bvsge s t)  (bvsle t s)
)))
