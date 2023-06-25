console.log("This is the JavaScript file");

const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  // console.log("searching...");
  let query = $("#search-input").val();
  if (query == "") {
    $(".search-result").hide();
  } else {
    console.log(query);

    let url = `http://localhost:8081/search/${query}`;
    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        console.log(data);
      });

    $(".search-result").show();
  }
};

//first request to server to create an order

const paymentStart = () => {
  console.log("Payment Started!");
  let amount = $("#payment_field").val();
  console.log(amount);
  if (amount == "" || amount == null) {
    alert("amount is required");
    return;
  }

  //code to create (AJAX request) a payment request to the server using jquery
  $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({ amount: amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      //invoked when success
      console.log(response);
      if (response.status == "created") {
        //now open the payment form
        let options = {
          key: "rzp_test_Alg9ssRXZSbXhA",
          amount: response.amount,
          currency: "INR",
          name: "ContactMate",
          description: "pay to ContactMate",
          image:
            "https://images.unsplash.com/photo-1579621970795-87facc2f976d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80",
          order_id: response.order_id,

          handler: function (response) {
            console.log(response.order_id);
            alert(response.razorpay_payment_id);
            alert(response.razorpay_order_id);
            alert(response.razorpay_signature);
            console.log("payment successful");
          },
          prefill: {
            //We recommend using the prefill parameter to auto-fill customer's contact information, especially their phone number
            name: "", //your customer's name
            email: "",
            contact: "", //Provide the customer's phone number for better conversion rates
          },
          notes: {
            address: "ContactMate Subscription",
          },
          theme: {
            color: "#3399cc",
          },
        };

        let rzp = new Razorpay(options);
        rzp.on("payment.failed", function (response) {
          console.log(response.error.code);
          console.log(response.error.description);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
          alert("payment failed");
        });
        rzp.open();
      }
    },
    error: function (error) {
      //invoked when error arises
      console.log(error);
      alert("Something went wrong");
    },
  });
};
