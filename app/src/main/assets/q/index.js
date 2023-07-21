function init(val) {

  window.survey = new Survey.Model(val);
  survey.onComplete.add(function(result) {

//    document
//                .querySelector('#surveyResult')
//                .textContent = "Result JSON:\n" + JSON.stringify(result.data, null, 3);
  showAndroidToast(JSON.stringify(result.data))
  });

  $("#surveyElement").Survey({
    model: survey
  });
}
function showAndroidToast(toast) {
        Android.showToast(toast);
}
if (!window["%hammerhead%"]) {
  //init();
}


