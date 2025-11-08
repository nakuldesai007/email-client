package com.emailclient.backend.email.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.65.1)",
    comments = "Source: email_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EmailServiceGrpc {

  private EmailServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.emailclient.backend.grpc.EmailService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListInboxRequest,
      com.emailclient.backend.email.grpc.ListInboxResponse> getListInboxMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListInbox",
      requestType = com.emailclient.backend.email.grpc.ListInboxRequest.class,
      responseType = com.emailclient.backend.email.grpc.ListInboxResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListInboxRequest,
      com.emailclient.backend.email.grpc.ListInboxResponse> getListInboxMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListInboxRequest, com.emailclient.backend.email.grpc.ListInboxResponse> getListInboxMethod;
    if ((getListInboxMethod = EmailServiceGrpc.getListInboxMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getListInboxMethod = EmailServiceGrpc.getListInboxMethod) == null) {
          EmailServiceGrpc.getListInboxMethod = getListInboxMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.ListInboxRequest, com.emailclient.backend.email.grpc.ListInboxResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListInbox"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListInboxRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListInboxResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("ListInbox"))
              .build();
        }
      }
    }
    return getListInboxMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListSentRequest,
      com.emailclient.backend.email.grpc.ListSentResponse> getListSentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListSent",
      requestType = com.emailclient.backend.email.grpc.ListSentRequest.class,
      responseType = com.emailclient.backend.email.grpc.ListSentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListSentRequest,
      com.emailclient.backend.email.grpc.ListSentResponse> getListSentMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListSentRequest, com.emailclient.backend.email.grpc.ListSentResponse> getListSentMethod;
    if ((getListSentMethod = EmailServiceGrpc.getListSentMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getListSentMethod = EmailServiceGrpc.getListSentMethod) == null) {
          EmailServiceGrpc.getListSentMethod = getListSentMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.ListSentRequest, com.emailclient.backend.email.grpc.ListSentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListSent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListSentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListSentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("ListSent"))
              .build();
        }
      }
    }
    return getListSentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListTrashRequest,
      com.emailclient.backend.email.grpc.ListTrashResponse> getListTrashMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTrash",
      requestType = com.emailclient.backend.email.grpc.ListTrashRequest.class,
      responseType = com.emailclient.backend.email.grpc.ListTrashResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListTrashRequest,
      com.emailclient.backend.email.grpc.ListTrashResponse> getListTrashMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.ListTrashRequest, com.emailclient.backend.email.grpc.ListTrashResponse> getListTrashMethod;
    if ((getListTrashMethod = EmailServiceGrpc.getListTrashMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getListTrashMethod = EmailServiceGrpc.getListTrashMethod) == null) {
          EmailServiceGrpc.getListTrashMethod = getListTrashMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.ListTrashRequest, com.emailclient.backend.email.grpc.ListTrashResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTrash"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListTrashRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.ListTrashResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("ListTrash"))
              .build();
        }
      }
    }
    return getListTrashMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.SendEmailRequestMessage,
      com.emailclient.backend.email.grpc.SendEmailResponse> getSendEmailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SendEmail",
      requestType = com.emailclient.backend.email.grpc.SendEmailRequestMessage.class,
      responseType = com.emailclient.backend.email.grpc.SendEmailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.SendEmailRequestMessage,
      com.emailclient.backend.email.grpc.SendEmailResponse> getSendEmailMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.SendEmailRequestMessage, com.emailclient.backend.email.grpc.SendEmailResponse> getSendEmailMethod;
    if ((getSendEmailMethod = EmailServiceGrpc.getSendEmailMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getSendEmailMethod = EmailServiceGrpc.getSendEmailMethod) == null) {
          EmailServiceGrpc.getSendEmailMethod = getSendEmailMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.SendEmailRequestMessage, com.emailclient.backend.email.grpc.SendEmailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SendEmail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.SendEmailRequestMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.SendEmailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("SendEmail"))
              .build();
        }
      }
    }
    return getSendEmailMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.GetEmailRequest,
      com.emailclient.backend.email.grpc.GetEmailResponse> getGetEmailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEmail",
      requestType = com.emailclient.backend.email.grpc.GetEmailRequest.class,
      responseType = com.emailclient.backend.email.grpc.GetEmailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.GetEmailRequest,
      com.emailclient.backend.email.grpc.GetEmailResponse> getGetEmailMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.GetEmailRequest, com.emailclient.backend.email.grpc.GetEmailResponse> getGetEmailMethod;
    if ((getGetEmailMethod = EmailServiceGrpc.getGetEmailMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getGetEmailMethod = EmailServiceGrpc.getGetEmailMethod) == null) {
          EmailServiceGrpc.getGetEmailMethod = getGetEmailMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.GetEmailRequest, com.emailclient.backend.email.grpc.GetEmailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEmail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.GetEmailRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.GetEmailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("GetEmail"))
              .build();
        }
      }
    }
    return getGetEmailMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.DeleteEmailRequest,
      com.emailclient.backend.email.grpc.DeleteEmailResponse> getDeleteEmailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteEmail",
      requestType = com.emailclient.backend.email.grpc.DeleteEmailRequest.class,
      responseType = com.emailclient.backend.email.grpc.DeleteEmailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.DeleteEmailRequest,
      com.emailclient.backend.email.grpc.DeleteEmailResponse> getDeleteEmailMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.DeleteEmailRequest, com.emailclient.backend.email.grpc.DeleteEmailResponse> getDeleteEmailMethod;
    if ((getDeleteEmailMethod = EmailServiceGrpc.getDeleteEmailMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getDeleteEmailMethod = EmailServiceGrpc.getDeleteEmailMethod) == null) {
          EmailServiceGrpc.getDeleteEmailMethod = getDeleteEmailMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.DeleteEmailRequest, com.emailclient.backend.email.grpc.DeleteEmailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteEmail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.DeleteEmailRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.DeleteEmailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("DeleteEmail"))
              .build();
        }
      }
    }
    return getDeleteEmailMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.MoveToTrashRequest,
      com.emailclient.backend.email.grpc.MoveToTrashResponse> getMoveToTrashMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MoveToTrash",
      requestType = com.emailclient.backend.email.grpc.MoveToTrashRequest.class,
      responseType = com.emailclient.backend.email.grpc.MoveToTrashResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.MoveToTrashRequest,
      com.emailclient.backend.email.grpc.MoveToTrashResponse> getMoveToTrashMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.MoveToTrashRequest, com.emailclient.backend.email.grpc.MoveToTrashResponse> getMoveToTrashMethod;
    if ((getMoveToTrashMethod = EmailServiceGrpc.getMoveToTrashMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getMoveToTrashMethod = EmailServiceGrpc.getMoveToTrashMethod) == null) {
          EmailServiceGrpc.getMoveToTrashMethod = getMoveToTrashMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.MoveToTrashRequest, com.emailclient.backend.email.grpc.MoveToTrashResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MoveToTrash"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.MoveToTrashRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.MoveToTrashResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("MoveToTrash"))
              .build();
        }
      }
    }
    return getMoveToTrashMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.PermanentlyDeleteRequest,
      com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> getPermanentlyDeleteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PermanentlyDelete",
      requestType = com.emailclient.backend.email.grpc.PermanentlyDeleteRequest.class,
      responseType = com.emailclient.backend.email.grpc.PermanentlyDeleteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.PermanentlyDeleteRequest,
      com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> getPermanentlyDeleteMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.PermanentlyDeleteRequest, com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> getPermanentlyDeleteMethod;
    if ((getPermanentlyDeleteMethod = EmailServiceGrpc.getPermanentlyDeleteMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getPermanentlyDeleteMethod = EmailServiceGrpc.getPermanentlyDeleteMethod) == null) {
          EmailServiceGrpc.getPermanentlyDeleteMethod = getPermanentlyDeleteMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.PermanentlyDeleteRequest, com.emailclient.backend.email.grpc.PermanentlyDeleteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PermanentlyDelete"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.PermanentlyDeleteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.PermanentlyDeleteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("PermanentlyDelete"))
              .build();
        }
      }
    }
    return getPermanentlyDeleteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.RestoreEmailRequest,
      com.emailclient.backend.email.grpc.RestoreEmailResponse> getRestoreEmailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RestoreEmail",
      requestType = com.emailclient.backend.email.grpc.RestoreEmailRequest.class,
      responseType = com.emailclient.backend.email.grpc.RestoreEmailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.RestoreEmailRequest,
      com.emailclient.backend.email.grpc.RestoreEmailResponse> getRestoreEmailMethod() {
    io.grpc.MethodDescriptor<com.emailclient.backend.email.grpc.RestoreEmailRequest, com.emailclient.backend.email.grpc.RestoreEmailResponse> getRestoreEmailMethod;
    if ((getRestoreEmailMethod = EmailServiceGrpc.getRestoreEmailMethod) == null) {
      synchronized (EmailServiceGrpc.class) {
        if ((getRestoreEmailMethod = EmailServiceGrpc.getRestoreEmailMethod) == null) {
          EmailServiceGrpc.getRestoreEmailMethod = getRestoreEmailMethod =
              io.grpc.MethodDescriptor.<com.emailclient.backend.email.grpc.RestoreEmailRequest, com.emailclient.backend.email.grpc.RestoreEmailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RestoreEmail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.RestoreEmailRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.emailclient.backend.email.grpc.RestoreEmailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EmailServiceMethodDescriptorSupplier("RestoreEmail"))
              .build();
        }
      }
    }
    return getRestoreEmailMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EmailServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EmailServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EmailServiceStub>() {
        @java.lang.Override
        public EmailServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EmailServiceStub(channel, callOptions);
        }
      };
    return EmailServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EmailServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EmailServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EmailServiceBlockingStub>() {
        @java.lang.Override
        public EmailServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EmailServiceBlockingStub(channel, callOptions);
        }
      };
    return EmailServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EmailServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EmailServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EmailServiceFutureStub>() {
        @java.lang.Override
        public EmailServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EmailServiceFutureStub(channel, callOptions);
        }
      };
    return EmailServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void listInbox(com.emailclient.backend.email.grpc.ListInboxRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListInboxResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListInboxMethod(), responseObserver);
    }

    /**
     */
    default void listSent(com.emailclient.backend.email.grpc.ListSentRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListSentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListSentMethod(), responseObserver);
    }

    /**
     */
    default void listTrash(com.emailclient.backend.email.grpc.ListTrashRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListTrashResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTrashMethod(), responseObserver);
    }

    /**
     */
    default void sendEmail(com.emailclient.backend.email.grpc.SendEmailRequestMessage request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.SendEmailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSendEmailMethod(), responseObserver);
    }

    /**
     */
    default void getEmail(com.emailclient.backend.email.grpc.GetEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.GetEmailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetEmailMethod(), responseObserver);
    }

    /**
     */
    default void deleteEmail(com.emailclient.backend.email.grpc.DeleteEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.DeleteEmailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteEmailMethod(), responseObserver);
    }

    /**
     */
    default void moveToTrash(com.emailclient.backend.email.grpc.MoveToTrashRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.MoveToTrashResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMoveToTrashMethod(), responseObserver);
    }

    /**
     */
    default void permanentlyDelete(com.emailclient.backend.email.grpc.PermanentlyDeleteRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPermanentlyDeleteMethod(), responseObserver);
    }

    /**
     */
    default void restoreEmail(com.emailclient.backend.email.grpc.RestoreEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.RestoreEmailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRestoreEmailMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service EmailService.
   */
  public static abstract class EmailServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return EmailServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service EmailService.
   */
  public static final class EmailServiceStub
      extends io.grpc.stub.AbstractAsyncStub<EmailServiceStub> {
    private EmailServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EmailServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EmailServiceStub(channel, callOptions);
    }

    /**
     */
    public void listInbox(com.emailclient.backend.email.grpc.ListInboxRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListInboxResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListInboxMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listSent(com.emailclient.backend.email.grpc.ListSentRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListSentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListSentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listTrash(com.emailclient.backend.email.grpc.ListTrashRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListTrashResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTrashMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sendEmail(com.emailclient.backend.email.grpc.SendEmailRequestMessage request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.SendEmailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSendEmailMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEmail(com.emailclient.backend.email.grpc.GetEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.GetEmailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetEmailMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteEmail(com.emailclient.backend.email.grpc.DeleteEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.DeleteEmailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteEmailMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void moveToTrash(com.emailclient.backend.email.grpc.MoveToTrashRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.MoveToTrashResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMoveToTrashMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void permanentlyDelete(com.emailclient.backend.email.grpc.PermanentlyDeleteRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPermanentlyDeleteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void restoreEmail(com.emailclient.backend.email.grpc.RestoreEmailRequest request,
        io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.RestoreEmailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRestoreEmailMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service EmailService.
   */
  public static final class EmailServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<EmailServiceBlockingStub> {
    private EmailServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EmailServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EmailServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.ListInboxResponse listInbox(com.emailclient.backend.email.grpc.ListInboxRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListInboxMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.ListSentResponse listSent(com.emailclient.backend.email.grpc.ListSentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListSentMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.ListTrashResponse listTrash(com.emailclient.backend.email.grpc.ListTrashRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTrashMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.SendEmailResponse sendEmail(com.emailclient.backend.email.grpc.SendEmailRequestMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSendEmailMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.GetEmailResponse getEmail(com.emailclient.backend.email.grpc.GetEmailRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetEmailMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.DeleteEmailResponse deleteEmail(com.emailclient.backend.email.grpc.DeleteEmailRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteEmailMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.MoveToTrashResponse moveToTrash(com.emailclient.backend.email.grpc.MoveToTrashRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMoveToTrashMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.PermanentlyDeleteResponse permanentlyDelete(com.emailclient.backend.email.grpc.PermanentlyDeleteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPermanentlyDeleteMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.emailclient.backend.email.grpc.RestoreEmailResponse restoreEmail(com.emailclient.backend.email.grpc.RestoreEmailRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRestoreEmailMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service EmailService.
   */
  public static final class EmailServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<EmailServiceFutureStub> {
    private EmailServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EmailServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EmailServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.ListInboxResponse> listInbox(
        com.emailclient.backend.email.grpc.ListInboxRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListInboxMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.ListSentResponse> listSent(
        com.emailclient.backend.email.grpc.ListSentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListSentMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.ListTrashResponse> listTrash(
        com.emailclient.backend.email.grpc.ListTrashRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTrashMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.SendEmailResponse> sendEmail(
        com.emailclient.backend.email.grpc.SendEmailRequestMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSendEmailMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.GetEmailResponse> getEmail(
        com.emailclient.backend.email.grpc.GetEmailRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetEmailMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.DeleteEmailResponse> deleteEmail(
        com.emailclient.backend.email.grpc.DeleteEmailRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteEmailMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.MoveToTrashResponse> moveToTrash(
        com.emailclient.backend.email.grpc.MoveToTrashRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMoveToTrashMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.PermanentlyDeleteResponse> permanentlyDelete(
        com.emailclient.backend.email.grpc.PermanentlyDeleteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPermanentlyDeleteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.emailclient.backend.email.grpc.RestoreEmailResponse> restoreEmail(
        com.emailclient.backend.email.grpc.RestoreEmailRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRestoreEmailMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_INBOX = 0;
  private static final int METHODID_LIST_SENT = 1;
  private static final int METHODID_LIST_TRASH = 2;
  private static final int METHODID_SEND_EMAIL = 3;
  private static final int METHODID_GET_EMAIL = 4;
  private static final int METHODID_DELETE_EMAIL = 5;
  private static final int METHODID_MOVE_TO_TRASH = 6;
  private static final int METHODID_PERMANENTLY_DELETE = 7;
  private static final int METHODID_RESTORE_EMAIL = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_INBOX:
          serviceImpl.listInbox((com.emailclient.backend.email.grpc.ListInboxRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListInboxResponse>) responseObserver);
          break;
        case METHODID_LIST_SENT:
          serviceImpl.listSent((com.emailclient.backend.email.grpc.ListSentRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListSentResponse>) responseObserver);
          break;
        case METHODID_LIST_TRASH:
          serviceImpl.listTrash((com.emailclient.backend.email.grpc.ListTrashRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.ListTrashResponse>) responseObserver);
          break;
        case METHODID_SEND_EMAIL:
          serviceImpl.sendEmail((com.emailclient.backend.email.grpc.SendEmailRequestMessage) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.SendEmailResponse>) responseObserver);
          break;
        case METHODID_GET_EMAIL:
          serviceImpl.getEmail((com.emailclient.backend.email.grpc.GetEmailRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.GetEmailResponse>) responseObserver);
          break;
        case METHODID_DELETE_EMAIL:
          serviceImpl.deleteEmail((com.emailclient.backend.email.grpc.DeleteEmailRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.DeleteEmailResponse>) responseObserver);
          break;
        case METHODID_MOVE_TO_TRASH:
          serviceImpl.moveToTrash((com.emailclient.backend.email.grpc.MoveToTrashRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.MoveToTrashResponse>) responseObserver);
          break;
        case METHODID_PERMANENTLY_DELETE:
          serviceImpl.permanentlyDelete((com.emailclient.backend.email.grpc.PermanentlyDeleteRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.PermanentlyDeleteResponse>) responseObserver);
          break;
        case METHODID_RESTORE_EMAIL:
          serviceImpl.restoreEmail((com.emailclient.backend.email.grpc.RestoreEmailRequest) request,
              (io.grpc.stub.StreamObserver<com.emailclient.backend.email.grpc.RestoreEmailResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getListInboxMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.ListInboxRequest,
              com.emailclient.backend.email.grpc.ListInboxResponse>(
                service, METHODID_LIST_INBOX)))
        .addMethod(
          getListSentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.ListSentRequest,
              com.emailclient.backend.email.grpc.ListSentResponse>(
                service, METHODID_LIST_SENT)))
        .addMethod(
          getListTrashMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.ListTrashRequest,
              com.emailclient.backend.email.grpc.ListTrashResponse>(
                service, METHODID_LIST_TRASH)))
        .addMethod(
          getSendEmailMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.SendEmailRequestMessage,
              com.emailclient.backend.email.grpc.SendEmailResponse>(
                service, METHODID_SEND_EMAIL)))
        .addMethod(
          getGetEmailMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.GetEmailRequest,
              com.emailclient.backend.email.grpc.GetEmailResponse>(
                service, METHODID_GET_EMAIL)))
        .addMethod(
          getDeleteEmailMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.DeleteEmailRequest,
              com.emailclient.backend.email.grpc.DeleteEmailResponse>(
                service, METHODID_DELETE_EMAIL)))
        .addMethod(
          getMoveToTrashMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.MoveToTrashRequest,
              com.emailclient.backend.email.grpc.MoveToTrashResponse>(
                service, METHODID_MOVE_TO_TRASH)))
        .addMethod(
          getPermanentlyDeleteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.PermanentlyDeleteRequest,
              com.emailclient.backend.email.grpc.PermanentlyDeleteResponse>(
                service, METHODID_PERMANENTLY_DELETE)))
        .addMethod(
          getRestoreEmailMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.emailclient.backend.email.grpc.RestoreEmailRequest,
              com.emailclient.backend.email.grpc.RestoreEmailResponse>(
                service, METHODID_RESTORE_EMAIL)))
        .build();
  }

  private static abstract class EmailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EmailServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.emailclient.backend.email.grpc.EmailServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EmailService");
    }
  }

  private static final class EmailServiceFileDescriptorSupplier
      extends EmailServiceBaseDescriptorSupplier {
    EmailServiceFileDescriptorSupplier() {}
  }

  private static final class EmailServiceMethodDescriptorSupplier
      extends EmailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    EmailServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EmailServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EmailServiceFileDescriptorSupplier())
              .addMethod(getListInboxMethod())
              .addMethod(getListSentMethod())
              .addMethod(getListTrashMethod())
              .addMethod(getSendEmailMethod())
              .addMethod(getGetEmailMethod())
              .addMethod(getDeleteEmailMethod())
              .addMethod(getMoveToTrashMethod())
              .addMethod(getPermanentlyDeleteMethod())
              .addMethod(getRestoreEmailMethod())
              .build();
        }
      }
    }
    return result;
  }
}
