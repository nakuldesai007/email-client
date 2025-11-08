package com.emailclient.backend.email.grpc;

import com.emailclient.backend.email.dto.EmailDetail;
import com.emailclient.backend.email.dto.EmailPreview;
import com.emailclient.backend.email.dto.SendEmailRequest;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.OffsetDateTime;
import java.util.List;

@GrpcService
public class EmailGrpcService extends EmailServiceGrpc.EmailServiceImplBase {

    private final com.emailclient.backend.email.EmailService emailService;

    public EmailGrpcService(com.emailclient.backend.email.EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void listInbox(ListInboxRequest request, StreamObserver<ListInboxResponse> responseObserver) {
        try {
            List<EmailPreview> previews = emailService.listInbox();
            ListInboxResponse response = ListInboxResponse.newBuilder()
                    .addAllEmails(previews.stream().map(this::toProto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to fetch inbox")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void listSent(ListSentRequest request, StreamObserver<ListSentResponse> responseObserver) {
        try {
            List<EmailPreview> previews = emailService.listSent();
            ListSentResponse response = ListSentResponse.newBuilder()
                    .addAllEmails(previews.stream().map(this::toProto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to fetch sent emails")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void sendEmail(SendEmailRequestMessage request, StreamObserver<SendEmailResponse> responseObserver) {
        try {
            SendEmailRequest domainRequest = new SendEmailRequest(
                    request.getTo(),
                    List.copyOf(request.getCcList()),
                    List.copyOf(request.getBccList()),
                    request.getSubject(),
                    request.getBody(),
                    List.copyOf(request.getAttachmentsList())
            );
            emailService.sendEmail(domainRequest);
            responseObserver.onNext(SendEmailResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to send email")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void getEmail(GetEmailRequest request, StreamObserver<GetEmailResponse> responseObserver) {
        try {
            emailService.getEmailDetail(request.getId())
                    .ifPresentOrElse(
                            detail -> {
                                GetEmailResponse response = GetEmailResponse.newBuilder()
                                        .setEmail(toDetailProto(detail))
                                        .build();
                                responseObserver.onNext(response);
                                responseObserver.onCompleted();
                            },
                            () -> responseObserver.onError(Status.NOT_FOUND
                                    .withDescription("Email not found")
                                    .asRuntimeException())
                    );
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to fetch email details")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void listTrash(ListTrashRequest request, StreamObserver<ListTrashResponse> responseObserver) {
        try {
            List<EmailPreview> previews = emailService.listTrash();
            ListTrashResponse response = ListTrashResponse.newBuilder()
                    .addAllEmails(previews.stream().map(this::toProto).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to fetch trash")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteEmail(DeleteEmailRequest request, StreamObserver<DeleteEmailResponse> responseObserver) {
        try {
            boolean success = emailService.deleteEmail(request.getId());
            DeleteEmailResponse response = DeleteEmailResponse.newBuilder()
                    .setSuccess(success)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to delete email")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void moveToTrash(MoveToTrashRequest request, StreamObserver<MoveToTrashResponse> responseObserver) {
        try {
            com.emailclient.backend.email.EmailService.MoveToTrashResult result = emailService.moveToTrash(request.getId());
            MoveToTrashResponse response = MoveToTrashResponse.newBuilder()
                    .setSuccess(result.success())
                    .setNewId(nullToEmpty(result.newId()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to move email to trash")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void restoreEmail(RestoreEmailRequest request, StreamObserver<RestoreEmailResponse> responseObserver) {
        try {
            com.emailclient.backend.email.EmailService.RestoreEmailResult result = emailService.restoreEmail(request.getId());
            RestoreEmailResponse response = RestoreEmailResponse.newBuilder()
                    .setSuccess(result.success())
                    .setNewId(nullToEmpty(result.newId()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to restore email")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    @Override
    public void permanentlyDelete(PermanentlyDeleteRequest request, StreamObserver<PermanentlyDeleteResponse> responseObserver) {
        try {
            boolean success = emailService.permanentlyDelete(request.getId());
            PermanentlyDeleteResponse response = PermanentlyDeleteResponse.newBuilder()
                    .setSuccess(success)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to permanently delete email")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    private EmailPreviewMessage toProto(EmailPreview preview) {
        EmailPreviewMessage.Builder builder = EmailPreviewMessage.newBuilder()
                .setId(nullToEmpty(preview.id()))
                .setFrom(nullToEmpty(preview.from()))
                .setSubject(nullToEmpty(preview.subject()))
                .setUnread(preview.unread());

        if (preview.receivedAt() != null) {
            builder.setReceivedAt(toTimestamp(preview.receivedAt()));
        }

        return builder.build();
    }

    private Timestamp toTimestamp(OffsetDateTime dateTime) {
        long seconds = dateTime.toInstant().getEpochSecond();
        int nanos = dateTime.getNano();
        return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private EmailDetailMessage toDetailProto(EmailDetail detail) {
        EmailDetailMessage.Builder builder = EmailDetailMessage.newBuilder()
                .setId(nullToEmpty(detail.id()))
                .setFrom(nullToEmpty(detail.from()))
                .setSubject(nullToEmpty(detail.subject()))
                .setBody(nullToEmpty(detail.body()))
                .setUnread(detail.unread())
                .addAllTo(detail.to())
                .addAllCc(detail.cc());

        if (detail.receivedAt() != null) {
            builder.setReceivedAt(toTimestamp(detail.receivedAt()));
        }

        return builder.build();
    }
}

