(benchmark smtaxiombvnand
 :logic QF_BV
 :extrafuns ((s BitVec[64]))
 :extrafuns ((t BitVec[64]))
 :formula (not (=
    (bvnand s t)  (bvnot (bvand s t))
)))
