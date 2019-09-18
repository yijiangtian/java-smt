(benchmark smtaxiombvsmod
 :logic QF_BV
 :extrafuns ((s BitVec[7]))
 :extrafuns ((t BitVec[7]))
 :formula (not (=
    (bvsmod s t) 
      (let (?msb_s (extract[6:6] s))
      (let (?msb_t (extract[6:6] t))
      (let (?nrm_s (ite ?msb_s (bvneg s) s))
      (let (?nrm_t (ite ?msb_t (bvneg t) t))
      (let (?nurem (bvurem ?nrm_s ?nrm_t))
      (let (?nuremzero (= ?nurem bv0[7]))
      (ite (and (= ?msb_s bit0) (= ?msb_t bit0))
           ?nurem
      (ite (and (= ?msb_s bit1) (= ?msb_t bit0))
           (ite ?nuremzero bv0[7] (bvadd (bvneg ?nurem) t))
      (ite (and (= ?msb_s bit0) (= ?msb_t bit1))
           (ite ?nuremzero bv0[7] (bvadd ?nurem t))
           (bvneg ?nurem))))))))))
)))
