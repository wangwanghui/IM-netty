function checkcompany(){
            //判断业务部门是否是黄石运营公司A03A05A02
             var deptId=$("input[name='oct_count_sign.business_depart_id']").val();
            if(deptId){
                $.ajax({
                		url: "purchaseController.do?queryOrgCode",
                		dataType: 'json',
                		type: 'post',
                		data: {
                            "id": deptId
                        },
                        async: false,
                		success: function(msg) {
                            var code=msg+"";
                			if(code.length>8){
                               console.log("进入if");
                			    if(code.indexOf("A03A05A02")!=-1){
                                    //黄石
                                    $("input[name*='dapart_type']").val("3");
                			    }else if(code.search("A03A05A01")!=-1){
                                    //常德
                                    $("input[name*='dapart_type']").val("2");
                                }else if(code.indexOf("A03A07")!=-1||code.search("A03A10")!=-1||code.search("A03A21")!=-1){
                                    //非运营公司 ，数字娱乐，卡乐技术，文旅建设
                                    $("input[name*='dapart_type']").val("1");
                                }else{
                                    //本部其他
                                $("input[name*='dapart_type']").val("0");
                            }
                			}else if(code.indexOf("A03A07")!=-1||code.search("A03A10")!=-1||code.search("A03A21")!=-1){
                                console.log("进入elseif")
                                    //非运营公司 ，数字娱乐，卡乐技术，文旅建设
                                    $("input[name*='dapart_type']").val("1");
                                }else{
                                    console.log(msg);
                                    console.log(code.indexOf("A03A07"));
                                    console.log("进入else");
                                    //本部其他
                                $("input[name*='dapart_type']").val("0");
                            }

                            var preSettlement = 0;
                            if(code.indexOf("A03A21")!=-1){ //为文旅建设
                                 preSettlement = 1;
                            }
                            $("#oct_count_sign_pre_settlement").val(preSettlement);
                		}
                	});
            }
  }