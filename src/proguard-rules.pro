# Add any ProGuard configurations specific to this
# extension here.

-keep public class xyz.kumaraswamy.textingitoo.TextingItoo {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'xyz/kumaraswamy/textingitoo/repack'
-flattenpackagehierarchy
-dontpreverify
